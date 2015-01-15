package org.ttdc.flipcards.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.ttdc.flipcards.client.StudyWordsService;
import org.ttdc.flipcards.shared.NotLoggedInException;
import org.ttdc.flipcards.shared.QuizOptions;
import org.ttdc.flipcards.shared.UserStat;
import org.ttdc.flipcards.shared.WordPair;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class StudyWordsServiceImpl extends RemoteServiceServlet implements
		StudyWordsService {
	private static final Logger LOG = Logger
			.getLogger(StudyWordsServiceImpl.class.getName());
	private static final PersistenceManagerFactory PMF = JDOHelper
			.getPersistenceManagerFactory("transactions-optional");

	// private static List<WordPair> wordPairs = new ArrayList<>();
	private static Map<String, WordPair> wordPairs = new HashMap<>();

	private PersistenceManager getPersistenceManager() {
		return PMF.getPersistenceManager();
	}

	private void checkLoggedIn() throws NotLoggedInException {
		if (getUser() == null) {
			throw new NotLoggedInException("Not logged in.");
		}
	}

	private User getUser() {
		UserService userService = UserServiceFactory.getUserService();
		return userService.getCurrentUser();
	}

	@Override
	public WordPair addWordPair(String word, String definition)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		UUID uuid = java.util.UUID.randomUUID();
		//
		// WordPair pair = new WordPair(uuid.toString(), word, definition);
		//
		// System.err.println("Created: " + pair.getId() + ": " + word);
		//
		// wordPairs.put(pair.getId(), pair);
		// checkLoggedIn();

		PersistenceManager pm = getPersistenceManager();

		try {
			PersistantWordPair pair = new PersistantWordPair(uuid.toString(), word, definition, "DICT_ID", getUser());
			pm.makePersistent(pair);
			return convert(pair);
		} catch (Exception e) {
			LOG.log(Level.WARNING, e.getMessage());
			return null;
		} finally {
			pm.close();
		}

	}
	
	WordPair convert(PersistantWordPair pair){
		WordPair gwtPair = new WordPair(pair.getId(), pair.getWord(), pair.getDefinition(), pair.getDictionaryId());
		gwtPair.setUser(pair.getUser().getNickname());
		return gwtPair;
	}

	@Override
	public List<WordPair> getAllWordPairs() throws NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		// List<String> symbols = new ArrayList<String>();
		// return new ArrayList<>(wordPairs.values());

		List<WordPair> wordPairs = new ArrayList<>();
		try {
			Query q = pm.newQuery(PersistantWordPair.class);
			// q.declareParameters("com.google.appengine.api.users.User u");
			// q.setOrdering("createDate");
			List<PersistantWordPair> result = (List<PersistantWordPair>) q.execute();
			for (PersistantWordPair pair : result) {
//				wordPairs.add(pm.detachCopy(pair));  //http://bpossolo.blogspot.com/2013/03/upgrading-gae-app-from-jpa1-to-jpa2.html
				wordPairs.add(convert(pair));
			}

		} finally {
			pm.close();
		}
		return wordPairs;
	}

	@Override
	public WordPair updateWordPair(String id, String word, String definition)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	
	/*
	 * (non-Javadoc)
	 * @see org.ttdc.flipcards.client.StudyWordsService#deleteWordPair(java.lang.String)
	 * 
	 * See: Using cross-group transactions in JDO and JPA
	 * https://cloud.google.com/appengine/docs/java/datastore/transactions
	 * I needed to do that to delete two different types of objects from one transaction
	 * 
	 */
	@Override
	public Boolean deleteWordPair(String id) throws IllegalArgumentException,
			NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		Transaction trans = pm.currentTransaction();
		long deleteCount = 0;
		try {
			Query q = pm.newQuery(PersistantWordPair.class, "id == identity");
			q.declareParameters("java.lang.String identity");
			List<PersistantWordPair> wordPairs = (List<PersistantWordPair>) q.execute(id);
			for (PersistantWordPair pair : wordPairs) {
				if (id.equals(pair.getId())) {
					deleteCount++;
					trans.begin();
					
					
					Query q2 = pm.newQuery(UserStat.class, "wordPairId == identity");
					q2.declareParameters("java.lang.String identity");
					List<UserStat> quizStats = (List<UserStat>) q2.execute(id);
					
					pm.deletePersistent(pair);
					for(UserStat stat : quizStats){
						pm.deletePersistent(stat);
					}
					trans.commit();
				}
			}
			if (deleteCount != 1) {
				LOG.log(Level.WARNING, "word pair deleted " + deleteCount
						+ " pairs. How is that possible?");
			}
		}
		catch (Exception e) {
			LOG.log(Level.WARNING, e.getMessage());
			trans.rollback();
		}
		finally {
			pm.close();
		}

		return deleteCount == 1;
	}
	
	@Override
	public void answerQuestion(String id, Boolean correct)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		Transaction trans = pm.currentTransaction();
		try {
			trans.begin();
			Query q = pm.newQuery(UserStat.class, "wordPairId == identity");
			q.declareParameters("java.lang.String identity");
			List<UserStat> quizStats = (List<UserStat>) q.execute(id);
			
			
			//Query for the word, and then put the dictionary id into the UserStat.  You need it there for the filtering to work
			
			UserStat userStat;
			if(quizStats == null || quizStats.size() == 0){
				userStat = new UserStat(getUser(), id);
				pm.makePersistent(userStat);
			} else if(quizStats.size() > 1) {
				LOG.log(Level.WARNING, "Failed to find stats for " + id +" multiple user stats.  Internal error.");
				throw new RuntimeException("Failed to process answer: "+id+"!");
			} else {
				userStat = quizStats.get(0);
			}
			userStat.setDateStamp(new Date());
			if(!correct){
				userStat.setIncorrectCount(userStat.getIncorrectCount() + 1);
			}
			userStat.setViewCount(userStat.getViewCount() + 1);
			userStat.setDifficulty((double)userStat.getIncorrectCount() / (double)userStat.getViewCount());
			trans.commit();
		}
		catch (Exception e) {
			trans.rollback();
			throw new RuntimeException("Failed to update score.");
		} finally {
			pm.close();
		}
		
		
	}
	
	@Override
	public List<WordPair> generateQuiz(QuizOptions quizOptions)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		List<WordPair> wordPairs = new ArrayList<>();
		List<UserStat> userStats = new ArrayList<>();
		
		try {
			
			Query q = pm.newQuery(UserStat.class, "user == u");
			q.declareParameters("com.google.appengine.api.users.User u");
			q.setOrdering("difficulty desc");
			
			if(quizOptions.getSize() > 0){
//				userStats = (List<UserStat>) q.execute(getUser(), quizOptions.getSize());
				q.setRange(0, quizOptions.getSize());
			} 
			userStats = (List<UserStat>) q.execute(getUser());
			
			
			List<String> wordPairIds = new ArrayList<>();
			for(UserStat stat : userStats){
				wordPairIds.add(stat.getWordPairId());
			}
			
			Query q2 = pm.newQuery(PersistantWordPair.class, ":p.contains(id)");
//			q2.execute(wordPairIds);
//			q2.declareParameters();
			List<PersistantWordPair> result = (List<PersistantWordPair>) q2.execute(wordPairIds);
			
			//This goofy map business is to insure that the results are in the same order that the id's are in.  I don't think that 'contains' guarantees that.
			Map<String, PersistantWordPair> wordPairMap = new HashMap<>();
			for (PersistantWordPair pair : result) {
				wordPairMap.put(pair.getId(), pair);
			}
			for(String id : wordPairIds){
				wordPairs.add(convert(wordPairMap.get(id)));
			}
			

			//User must be new, or the app is new.  Lets get some words for them. But we don't want dups!
			if(wordPairs.size() < quizOptions.getSize()){
				//Getting the total number of words requested
				Query q3 = pm.newQuery(PersistantWordPair.class);
				if(quizOptions.getSize() > 0){
					q3.setRange(0, quizOptions.getSize());
				}
				result = (List<PersistantWordPair>)q3.execute();
				//Then skip the ones that were already added.
				for (PersistantWordPair pair : result) {
					if(!wordPairIds.contains(pair.getId())){
						wordPairs.add(convert(pair));
					}
				}
			}
			
			
//			Query q = pm.newQuery(PersistantWordPair.class);
//			// q.declareParameters("com.google.appengine.api.users.User u");
//			// q.setOrdering("createDate");
//			List<PersistantWordPair> result = (List<PersistantWordPair>) q.execute(quizOptions.getSize());
//			
//			for (PersistantWordPair pair : result) {
////				wordPairs.add(pm.detachCopy(pair));
//				wordPairs.add(convert(pair));
//			}
		} finally {
			pm.close();
		}
		return wordPairs;
	}
}
