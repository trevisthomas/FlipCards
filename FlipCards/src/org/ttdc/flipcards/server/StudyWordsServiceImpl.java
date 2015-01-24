package org.ttdc.flipcards.server;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.eclipse.jetty.util.log.Log;
import org.ttdc.flipcards.client.StudyWordsService;

import org.ttdc.flipcards.shared.NotLoggedInException;
import org.ttdc.flipcards.shared.QuizOptions;
import org.ttdc.flipcards.shared.Tag;
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
	// private static Map<String, WordPair> wordPairs = new HashMap<>();

	public static PersistenceManager getPersistenceManager() {
		return PMF.getPersistenceManager();
	}

	public static void checkLoggedIn() throws NotLoggedInException {
		if (getUser() == null) {
			throw new NotLoggedInException("Not logged in.");
		}
	}

	
	public static User getUser() {
		UserService userService = UserServiceFactory.getUserService();
		User currentUser = userService.getCurrentUser();
		if(currentUser != null){
			UploadService.lastUser = currentUser;
		} 
		return currentUser;
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
		if(exists(word, getUser()) != null){
			throw new IllegalArgumentException("Coudld not be added.  A card with this term already exists.");
		}

		PersistenceManager pm = getPersistenceManager();

		try {
			Card pair = new Card(uuid.toString(), word, definition, getUser());
			pm.makePersistent(pair);
			pm.flush();
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
	
	WordPair convert(Card pair) throws NotLoggedInException{
		
		
//		WordPair gwtPair = new WordPair(pair.getId(), pair.getWord(),
//				pair.getDefinition());
		
		WordPair gwtPair;
		
		gwtPair = new WordPair(pair.getId(), pair.getWord(),
				pair.getDefinition());
		
		
		gwtPair.setCorrectCount(pair.getViewCount() - pair.getIncorrectCount());
		gwtPair.setTestedCount(pair.getViewCount());
		gwtPair.setDifficulty(pair.getDifficulty());
		
		Map<String,Tag> allTags = getAllTagsMap();
		List<TagAssociation> tagAssociations = getTagAssociations(pair.getId());

		for(TagAssociation tagAss : tagAssociations){
			Tag tag = allTags.get(tagAss.getTagId());
			if(tag != null){ //This happened in dev after mucking with the db schema. I have a Tag with a null tagId in the ass db.
				gwtPair.getTags().add(tag);
			}
		}
		if(pair.getUser() == null){
			LOG.info("Tried to convert a card with a null user some how. CardId: " + pair.getId());
		} else {
			gwtPair.setUser(pair.getUser().getNickname());
		}
		return gwtPair;
	}

//	WordPair convert(Card pair) throws NotLoggedInException{
////		WordPair gwtPair;
////		if (switchEm) {
////			gwtPair = new WordPair(pair.getId(), pair.getDefinition(),
////					pair.getWord());
////		} else {
////			gwtPair = new WordPair(pair.getId(), pair.getWord(),
////					pair.getDefinition());
////		}
////		if(pair.getUser() != null){
////			gwtPair.setUser(pair.getUser().getNickname());
////		}
////		return gwtPair;
//		return convert(pair, false);
//		
//	}

	@Override
	public List<WordPair> getAllWordPairs() throws NotLoggedInException {
		checkLoggedIn();
		
		PersistenceManager pm = getPersistenceManager();
		List<WordPair> wordPairs = new ArrayList<>();
		try {
			Query q = pm.newQuery(Card.class, "user == u");
			q.declareParameters("com.google.appengine.api.users.User u");
			q.setOrdering("createDate");
			List<Card> cards = (List<Card>) q.execute(getUser());
			int i = 1;
			for (Card card : cards) {
				WordPair pair = convert(card);
				pair.setDisplayOrder(i++);
				wordPairs.add(pair);
			}

		} finally {
			pm.close();
		}
		return wordPairs;
	}
	
	@Override
	public List<WordPair> getWordPairs(List<String> tagIds)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		
		List<Card> cards = getCards(tagIds);
		
		List<WordPair> wordPairs = new ArrayList<>();
		int i = 1;
		for (Card card : cards) {
			WordPair pair = convert(card);
			pair.setDisplayOrder(i++);
			wordPairs.add(pair);
		}
		
		return wordPairs;
	}

	private List<Card> getCards(List<String> tagIds) throws NotLoggedInException {
		Set<Card> cardSet = new HashSet<>();
		for(String tagId : tagIds){
			List<Card> list = getCards(tagId);
			cardSet.addAll(list); //Probably should de dup.  Ok the set should de dup
		}
		return new ArrayList<>(cardSet);
	}
	
	public List<Card> getCards(String tagId)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		
		PersistenceManager pm = getPersistenceManager();
		List<Card> wordPairs = new ArrayList<>();
		try {
			Query q = pm.newQuery(TagAssociation.class, "user == u && tagId == tid");
			q.declareParameters("com.google.appengine.api.users.User u, java.lang.String tid");
			List<TagAssociation> tagAsses = (List<TagAssociation>) q.execute(getUser(), tagId);

			List<String> cardIds = new ArrayList<>();
			for(TagAssociation ta : tagAsses){
				cardIds.add(ta.getCardId());
			}
			
			if(cardIds.isEmpty()){
				return wordPairs;
			}
			
			Query q2 = pm.newQuery(Card.class, ":p.contains(id)");
			q2.setOrdering("createDate");
			List<Card> cards = (List<Card>) q2.execute(cardIds);
			return cards;

		} finally {
			pm.close();
		}
	}

	@Override
	public WordPair updateWordPair(String id, String word, String definition)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		Transaction trans = pm.currentTransaction();
		
		Card exists = exists(word, getUser());
		//Does it exist, and is not me!
		if(exists != null && !exists.getId().equals(id)){
			throw new IllegalArgumentException("Already exists");
		}
		
		try {
			Query q = pm.newQuery(Card.class, "user == u && id == i");
			q.declareParameters("com.google.appengine.api.users.User u, java.lang.String i");
			List<Card> cards = (List<Card>) q.execute(getUser(), id);
			trans.begin();
			Card card = cards.get(0);
			card.setWord(word);
			card.setDefinition(definition);
			trans.commit();
			
			return convert(card);
		} catch (Exception e) {
			trans.rollback();
			LOG.log(Level.WARNING, e.getMessage());
			throw e;
		} finally {
			pm.close();
		}

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
			trans.begin();
			for (Card pair : wordPairs) {
				if (id.equals(pair.getId())) {
					pm.deletePersistent(pair);
					deleteCount++;
				}
			}
			
			Query q2 = pm.newQuery(TagAssociation.class, "cardId == cid");
			q2.declareParameters("java.lang.String cid");
			LOG.info("Also deleted " +q2.deletePersistentAll(id) + " tag associations.");
			
			trans.commit();
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
	
	public static Card exists(String term, User user){
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Card.class, "user == u && word == w");
			q.declareParameters("com.google.appengine.api.users.User u, java.lang.String w");
			
			List<Card> cards = (List<Card>) q.execute(user, term);
			
			if(cards.isEmpty()){
				return null;
			}
			
			
			return cards.get(0);
		} catch (Exception e) {
			LOG.log(Level.WARNING, e.getMessage());
		} finally {
			pm.close();
		}
		return null;
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

			List<Card> cards = new ArrayList<>();
			if(quizOptions.getTagIds().isEmpty()){
				Query q = pm.newQuery(Card.class, "user == u");
				q.declareParameters("com.google.appengine.api.users.User u");
				cards = (List<Card>) q.execute(getUser());
			} else {
				cards = getCards(quizOptions.getTagIds());
			}
			
			disconnectedCards = new ArrayList<>(cards.size()); //Removing the cards from the persistant container so that i can sort.
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
//				switch (quizOptions.getCardSide()) {
//				case DEFINITION:
//					switchEm = true;
//					break;
//				case RANDOM:
//					switchEm = random.nextBoolean();
//					break;
//				case TERM:
//					switchEm = false;
//					break;
//				}
				wordPairs.add(convert(card));
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
	
	@Override
	public void applyTag(String tagId, String cardId)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		try{
			TagAssociation tag = new TagAssociation(getUser(), tagId, cardId);
			pm.makePersistent(tag);
		} catch (Exception e) {
			LOG.info(e.getMessage());
			throw new IllegalArgumentException("Failed to add tag");
		}
		finally {
			pm.close();
		}
	}
	
	@Override
	public Tag createTag(String name)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		try{
			UUID uuid = java.util.UUID.randomUUID();
			PersistantTagName tagName = new PersistantTagName(getUser(), uuid.toString(), name);
			pm.makePersistent(tagName);
			return new Tag(tagName.getTagId(), tagName.getTagName());
		} catch (Exception e) {
			LOG.info(e.getMessage());
			throw new IllegalArgumentException("Failed to add tag");
		}
		finally {
			pm.close();
		}
	}
	
	@Override
	public Tag updateTagName(String tagId, String name)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		
		PersistenceManager pm = getPersistenceManager();
		List<Tag> tags = new ArrayList<>();
		try {
			Query q = pm.newQuery(PersistantTagName.class, "tagId == i");
			q.declareParameters("java.lang.String i");
			List<PersistantTagName> serverTags  = (List<PersistantTagName>) q.execute(tagId);
			PersistantTagName tagName = serverTags.get(0);
			if(tagName != null){
				tagName.setTagName(name);
			}
			pm.flush();
			return new Tag(tagName.getTagId(), tagName.getTagName());

		} catch (Exception e) {
			LOG.info(e.getMessage());
			throw new IllegalArgumentException("Failed to retrieve");
		} finally {
			pm.close();
		}
	}
	
	@Override
	public void deleteTagName(String tagId) throws IllegalArgumentException,
			NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		Transaction trans = pm.currentTransaction();
		try{
			Query q = pm.newQuery(TagAssociation.class, "tagId == i");
			q.declareParameters("java.lang.String i");
			
			Query q2 = pm.newQuery(PersistantTagName.class, "tagId == i");
			q2.declareParameters("java.lang.String i");
			
			trans.begin();
				
			LOG.info("Deleted: "+q.deletePersistentAll(tagId) + " tags...");
			LOG.info("Deleted: "+q2.deletePersistentAll(tagId) + " tagNames");
			
			trans.commit();
		} catch (Exception e) {
			trans.rollback();
			LOG.info(e.getMessage());
			throw new IllegalArgumentException("faild tag opperation");
		}
		finally {
			pm.flush();
			pm.close();
		}
	}
	
	@Override
	public void deTag(String tagId, String cardId)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		try{
			Query q = pm.newQuery(TagAssociation.class, "tagId == tid && cardId == cid");
			q.declareParameters("java.lang.String tid, java.lang.String cid");
			long deleted = q.deletePersistentAll(tagId, cardId);
			LOG.info("Detaged: "+deleted + " items");
		} catch (Exception e) {
			LOG.info(e.getMessage());
			throw new IllegalArgumentException("Failed to remove tag");
		}
		finally {
			pm.flush();
			pm.close();
		}
	}
	
	/**
	 * Created for loading tag associations.  
	 * 
	 * Should i have just created an index and let google do this?
	 */
	public Map<String, Tag> getAllTagsMap() throws IllegalArgumentException,
			NotLoggedInException{
		Map<String, Tag> map = new HashMap<>();
		List<Tag> tags = getAllTagNames();
		for(Tag tag : tags){
			map.put(tag.getTagId(), tag);
		}
		return map;
	}
	
	
	
	@Override
	public List<Tag> getAllTagNames() throws IllegalArgumentException,
			NotLoggedInException {
		checkLoggedIn();
		
		PersistenceManager pm = getPersistenceManager();
		List<Tag> tags = new ArrayList<>();
		try {
			Query q = pm.newQuery(PersistantTagName.class);
			List<PersistantTagName> serverTagNames  = (List<PersistantTagName>) q.execute();
			
			for(PersistantTagName serverTagName : serverTagNames){
				tags.add(new Tag(serverTagName.getTagId(), serverTagName.getTagName()));
			}

		} catch (Exception e) {
			LOG.info(e.getMessage());
			throw new IllegalArgumentException("Failed to retrieve");
		} finally {
			pm.flush();
			pm.close();
		}
		return tags;
	}
	
	public List<TagAssociation> getTagAssociations(String cardId) throws IllegalArgumentException,
			NotLoggedInException {
		checkLoggedIn();
		
		PersistenceManager pm = getPersistenceManager();
		List<TagAssociation> tagAsses = new ArrayList<>();
		
		try {
			
			Query q = pm.newQuery(TagAssociation.class, "user == u && cardId == cid");
			q.declareParameters("com.google.appengine.api.users.User u, java.lang.String cid");
			tagAsses  = (List<TagAssociation>) q.execute(getUser(), cardId);
			
		} catch (Exception e) {
			LOG.info(e.getMessage());
			throw new IllegalArgumentException("Failed to retrieve tag associations");
		} finally {
			pm.close();
		}
		return tagAsses;
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
