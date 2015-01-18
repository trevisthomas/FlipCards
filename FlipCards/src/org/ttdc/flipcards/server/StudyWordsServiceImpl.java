package org.ttdc.flipcards.server;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
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
import org.w3c.dom.ranges.RangeException;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.client.Random;
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
	public String getFileUploadUrl() throws NotLoggedInException {
		BlobstoreService blobstoreService = BlobstoreServiceFactory
				.getBlobstoreService();
		return blobstoreService.createUploadUrl("/flipcards/upload");
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
			Card pair = new Card(uuid.toString(), word, definition, getUser());
			pm.makePersistent(pair);
			return convert(pair);
		} catch (Exception e) {
			LOG.log(Level.WARNING, e.getMessage());
			return null;
		} finally {
			pm.close();
		}
	}

	@Override
	public void assignSelfToUserlessWords() throws NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		Transaction trans = pm.currentTransaction();
		try {
			Query q = pm.newQuery(Card.class);
			List<Card> result = (List<Card>) q.execute();

			for (Card pair : result) {
				// If the user is null, fix it!
				if (pair.getUser() == null) {
					/*
					 * This is a hack because i cant figure out how to get the
					 * logged in user from the upload servlet so if i see those
					 * i fix them here.
					 */
					trans.begin();
					pair.setUser(getUser());
					trans.commit();
				}
			}

		} catch (Exception e) {
			LOG.log(Level.WARNING, e.getMessage());
			trans.rollback();
		} finally {
			pm.close();
		}
	}

	WordPair convert(Card pair) {
		WordPair gwtPair = new WordPair(pair.getId(), pair.getWord(),
				pair.getDefinition());

		gwtPair.setUser(pair.getUser().getNickname());
		return gwtPair;
	}

	WordPair convert(Card pair, boolean switchEm) {
		WordPair gwtPair;
		if (switchEm) {
			gwtPair = new WordPair(pair.getId(), pair.getDefinition(),
					pair.getWord());
		} else {
			gwtPair = new WordPair(pair.getId(), pair.getWord(),
					pair.getDefinition());
		}
		gwtPair.setUser(pair.getUser().getNickname());
		return gwtPair;
	}

	@Override
	public List<WordPair> getAllWordPairs(String dictionaryId)
			throws NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		// List<String> symbols = new ArrayList<String>();
		// return new ArrayList<>(wordPairs.values());

		List<WordPair> wordPairs = new ArrayList<>();
		try {
			// Query q = pm.newQuery(Card.class, "dictionaryId == d");
			// q.declareParameters("java.lang.String d");
			Query q = pm.newQuery(Card.class);
			q.setOrdering("createDate");
			List<Card> result = (List<Card>) q.execute();
			for (Card pair : result) {
				// wordPairs.add(pm.detachCopy(pair));
				// //http://bpossolo.blogspot.com/2013/03/upgrading-gae-app-from-jpa1-to-jpa2.html
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
	 * 
	 * @see
	 * org.ttdc.flipcards.client.StudyWordsService#deleteWordPair(java.lang.
	 * String)
	 * 
	 * See: Using cross-group transactions in JDO and JPA
	 * https://cloud.google.com/appengine/docs/java/datastore/transactions I
	 * needed to do that to delete two different types of objects from one
	 * transaction
	 */
	@Override
	public Boolean deleteWordPair(String id) throws IllegalArgumentException,
			NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		Transaction trans = pm.currentTransaction();
		long deleteCount = 0;
		try {
			Query q = pm.newQuery(Card.class, "id == identity");
			q.declareParameters("java.lang.String identity");
			List<Card> wordPairs = (List<Card>) q.execute(id);
			for (Card pair : wordPairs) {
				if (id.equals(pair.getId())) {
					pm.deletePersistent(pair);
					deleteCount++;
				}
			}
			if (deleteCount != 1) {
				LOG.log(Level.WARNING, "word pair deleted " + deleteCount
						+ " pairs. How is that possible?");
			}
		} catch (Exception e) {
			LOG.log(Level.WARNING, e.getMessage());
			trans.rollback();
		} finally {
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
			Query q = pm.newQuery(Card.class, "id == identity");
			q.declareParameters("java.lang.String identity");
			List<Card> quizStats = (List<Card>) q.execute(id);

			if (quizStats.size() != 1) {
				throw new RuntimeException("Failed to update score.");
			}

			// Query for the word, and then put the dictionary id into the
			// UserStat. You need it there for the filtering to work

			Card card = quizStats.get(0);
			if (!correct) {
				card.setIncorrectCount(card.getIncorrectCount() + 1);
			}
			card.setLastUpdate(new Date());
			card.setViewCount(card.getViewCount() + 1);
			card.setDifficulty((double) card.getIncorrectCount()
					/ (double) card.getViewCount());
			trans.commit();

			// UserStat userStat;
			// if(quizStats == null || quizStats.size() == 0){
			// userStat = new UserStat(getUser(), id);
			// pm.makePersistent(userStat);
			// } else if(quizStats.size() > 1) {
			// LOG.log(Level.WARNING, "Failed to find stats for " + id
			// +" multiple user stats.  Internal error.");
			// throw new RuntimeException("Failed to process answer: "+id+"!");
			// } else {
			// userStat = quizStats.get(0);
			// }
			// userStat.setDateStamp(new Date());
			// if(!correct){
			// userStat.setIncorrectCount(userStat.getIncorrectCount() + 1);
			// }
			// userStat.setViewCount(userStat.getViewCount() + 1);
			// userStat.setDifficulty((double)userStat.getIncorrectCount() /
			// (double)userStat.getViewCount());
			// trans.commit();
		} catch (Exception e) {
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
		java.util.Random random = new java.util.Random();

		List<WordPair> wordPairs = new ArrayList<>();
		List<Card> disconnectedCards;
		try {

			Query q = pm.newQuery(Card.class, "user == u");
			List<Card> cards = (List<Card>) q.execute(getUser());
			disconnectedCards = new ArrayList<>(cards.size());
			for (Card c : cards) {
				disconnectedCards.add(c);
			}

			switch (quizOptions.getCardOrder()) {
			case EASIEST:
				Collections.sort(disconnectedCards,
						new Card.CardSortDifficulty());
				break;
			case HARDEST:
				Collections.sort(disconnectedCards,
						new Card.CardSortDifficultyDesc());
				break;
			case LATEST_ADDED:
				Collections.sort(disconnectedCards,
						new Card.CardSortCreateDateDesc());
				break;
			case LEAST_STUDIED:
				Collections.sort(disconnectedCards,
						new Card.CardSortStudyCount());
				break;
			case RANDOM:
				Collections.shuffle(disconnectedCards);
				break;
			}

			LOG.info("Cards");
			int count = 0;
			boolean switchEm = false;
			for (Card card : disconnectedCards) {
				if (count++ == quizOptions.getSize()) {
					break;
				}
				switch (quizOptions.getCardSide()) {
				case DEFINITION:
					switchEm = false;
					break;
				case RANDOM:
					switchEm = random.nextBoolean();
					break;
				case TERM:
					switchEm = true;
					break;
				}
				wordPairs.add(convert(card, switchEm));
				LOG.info(card.toString());
			}

		} catch (Exception e) {
			// throw new RuntimeException("Failed to update score.");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			LOG.info(sw.toString());
			throw new RuntimeException("Internal server error.");
		}

		finally {
			pm.close();
		}
		return wordPairs;
	}

	// @Override
	// public List<WordPair> generateQuiz(QuizOptions quizOptions)
	// throws IllegalArgumentException, NotLoggedInException {
	// checkLoggedIn();
	// PersistenceManager pm = getPersistenceManager();
	// List<WordPair> wordPairs = new ArrayList<>();
	// List<UserStat> userStats = new ArrayList<>();
	//
	// try {
	// Query q = pm.newQuery(Card.class, "user == u && dictionaryId == d");
	// q.declareParameters("com.google.appengine.api.users.User u, java.lang.String d");
	// q.setOrdering("difficulty desc");
	//
	// if(quizOptions.getSize() > 0){
	// q.setRange(0, quizOptions.getSize());
	// }
	// List<Card> cards = (List<Card>) q.execute(getUser(),
	// /*quizOptions.getDictionaryId()*/ "default_dict");
	// for (Card card : cards) {
	// wordPairs.add(convert(card));
	// }
	// } catch (Exception e) {
	// //throw new RuntimeException("Failed to update score.");
	// StringWriter sw = new StringWriter();
	// PrintWriter pw = new PrintWriter(sw);
	// e.printStackTrace(pw);
	// LOG.warning(sw.toString());
	// throw new RuntimeException("Internal server error.");
	// }
	//
	// finally {
	// pm.close();
	// }
	// return wordPairs;
	// }

	// @Override
	// public List<WordPair> generateQuiz(QuizOptions quizOptions)
	// throws IllegalArgumentException, NotLoggedInException {
	// checkLoggedIn();
	// PersistenceManager pm = getPersistenceManager();
	// List<WordPair> wordPairs = new ArrayList<>();
	// List<UserStat> userStats = new ArrayList<>();
	//
	// try {
	//
	// Query q = pm.newQuery(UserStat.class, "user == u");
	// q.declareParameters("com.google.appengine.api.users.User u");
	// q.setOrdering("difficulty desc");
	//
	// if(quizOptions.getSize() > 0){
	// // userStats = (List<UserStat>) q.execute(getUser(),
	// quizOptions.getSize());
	// q.setRange(0, quizOptions.getSize());
	// }
	// userStats = (List<UserStat>) q.execute(getUser());
	//
	//
	// List<String> wordPairIds = new ArrayList<>();
	// for(UserStat stat : userStats){
	// wordPairIds.add(stat.getWordPairId());
	// }
	//
	// Query q2 = pm.newQuery(Card.class, ":p.contains(id)");
	// // q2.execute(wordPairIds);
	// // q2.declareParameters();
	// List<Card> result = (List<Card>) q2.execute(wordPairIds);
	//
	// //This goofy map business is to insure that the results are in the same
	// order that the id's are in. I don't think that 'contains' guarantees
	// that.
	// Map<String, Card> wordPairMap = new HashMap<>();
	// for (Card pair : result) {
	// wordPairMap.put(pair.getId(), pair);
	// }
	// for(String id : wordPairIds){
	// wordPairs.add(convert(wordPairMap.get(id)));
	// }
	//
	//
	// //User must be new, or the app is new. Lets get some words for them. But
	// we don't want dups!
	// if(wordPairs.size() < quizOptions.getSize()){
	// //Getting the total number of words requested
	// Query q3 = pm.newQuery(Card.class);
	// if(quizOptions.getSize() > 0){
	// q3.setRange(0, quizOptions.getSize());
	// }
	// result = (List<Card>)q3.execute();
	// //Then skip the ones that were already added.
	// for (Card pair : result) {
	// if(!wordPairIds.contains(pair.getId())){
	// wordPairs.add(convert(pair));
	// }
	// }
	// }
	//
	//
	// // Query q = pm.newQuery(Card.class);
	// // // q.declareParameters("com.google.appengine.api.users.User u");
	// // // q.setOrdering("createDate");
	// // List<Card> result = (List<Card>) q.execute(quizOptions.getSize());
	// //
	// // for (Card pair : result) {
	// //// wordPairs.add(pm.detachCopy(pair));
	// // wordPairs.add(convert(pair));
	// // }
	// } finally {
	// pm.close();
	// }
	// return wordPairs;
	// }
}
