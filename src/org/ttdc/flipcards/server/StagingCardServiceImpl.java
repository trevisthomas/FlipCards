package org.ttdc.flipcards.server;

import java.util.ArrayList;
import java.util.Collection;
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

import org.ttdc.flipcards.client.services.StagingCardService;
import org.ttdc.flipcards.shared.Constants;
import org.ttdc.flipcards.shared.NotLoggedInException;
import org.ttdc.flipcards.shared.PagedWordPair;
import org.ttdc.flipcards.shared.Tag;
import org.ttdc.flipcards.shared.WordPair;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.datanucleus.query.JDOCursorHelper;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class StagingCardServiceImpl extends RemoteServiceServlet implements
		StagingCardService {

	private static final Logger LOG = Logger
			.getLogger(StudyWordsServiceImpl.class.getName());

	@Override
	public String getFileUploadUrl() throws NotLoggedInException {
		BlobstoreService blobstoreService = BlobstoreServiceFactory
				.getBlobstoreService();
		return blobstoreService.createUploadUrl("/flipcards/upload");
	}

	@Override
	public WordPair add(String term, String definition)
			throws IllegalArgumentException, NotLoggedInException {
		StudyWordsServiceImpl.checkLoggedIn();
		UUID uuid = java.util.UUID.randomUUID();

		checkDoesTermExist(term, getUser());

		PersistenceManager pm = StudyWordsServiceImpl.getPersistenceManager();

		try {
			CardStaging card = new CardStaging(uuid.toString(), term,
					definition, getUser().getEmail());
			pm.makePersistent(card);
			return convert(card);
		} catch (Exception e) {
			LOG.log(Level.WARNING, e.getMessage());
			return null;
		} finally {
			pm.close();
		}
	}

	private User getUser() {
		return StudyWordsServiceImpl.getUser();
	}

	@Override
	public List<WordPair> getStagedCards(String owner)
			throws IllegalArgumentException, NotLoggedInException {
		StudyWordsServiceImpl.checkLoggedIn();

		PersistenceManager pm = StudyWordsServiceImpl.getPersistenceManager();
		List<WordPair> wordPairs = new ArrayList<>();
		try {

			// getConvertStagedUsers();

			Query q = pm.newQuery(CardStaging.class, "owner == o");
			q.declareParameters("java.lang.String o");
			q.setOrdering("createDate");
			List<CardStaging> cards = (List<CardStaging>) q.execute(owner
					.trim().isEmpty() ? getUser().getEmail() : owner);

			// //Until i figure out how to import as a user, the user field will
			// be null for imported ones.
			// Query q = pm.newQuery(CardStaging.class);
			// q.setOrdering("createDate");
			// List<CardStaging> cards = (List<CardStaging>) q.execute();

			int i = 1;
			for (CardStaging card : cards) {
				WordPair pair = convert(card);
				pair.setDisplayOrder(i++);
				wordPairs.add(pair);
			}

		} finally {
			pm.close();
		}

		// DEBUG TEST
		getStagedCards(owner, null);
		return wordPairs;
	}

	@Override
	public PagedWordPair getStagedCards(String owner, String cursorString)
			throws IllegalArgumentException, NotLoggedInException {
		StudyWordsServiceImpl.checkLoggedIn();

		PagedWordPair pagedWordPair = new PagedWordPair();
		PersistenceManager pm = StudyWordsServiceImpl.getPersistenceManager();
		List<WordPair> wordPairs = new ArrayList<>();
		try {

			// getConvertStagedUsers();

			Query q = pm.newQuery(CardStaging.class, "owner == o");
			q.declareParameters("java.lang.String o");
			q.setOrdering("createDate desc");

			String newCursorString = "";

			if (cursorString != null && !cursorString.isEmpty()) {
				Cursor cursor = Cursor.fromWebSafeString(cursorString);
				Map<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				q.setExtensions(extensionMap);
			} else {
				long count = countStagedCards(owner);
				pagedWordPair.setTotalCardCount(count);

			}

			q.setRange(0, Constants.PAGE_SIZE);

			List<CardStaging> cards = (List<CardStaging>) q.execute(owner
					.trim().isEmpty() ? getUser().getEmail() : owner);

			Cursor cursor = JDOCursorHelper.getCursor(cards);
			newCursorString = cursor.toWebSafeString();

			int i = 1;
			for (CardStaging card : cards) {
				WordPair pair = convert(card);
				pair.setDisplayOrder(i++);
				wordPairs.add(pair);
			}

			pagedWordPair.setCursorString(newCursorString);
			pagedWordPair.setWordPair(wordPairs);

			return pagedWordPair;

		} finally {
			pm.close();
		}

	}

	// @Override
	public long countStagedCards(String owner) throws IllegalArgumentException,
			NotLoggedInException {
		StudyWordsServiceImpl.checkLoggedIn();

		PersistenceManager pm = StudyWordsServiceImpl.getPersistenceManager();
		try {
			Query q = pm.newQuery(CardStaging.class, "owner == o");
			q.declareParameters("java.lang.String o");
			q.setResult("count(this)");
			Long result = (Long) q.execute(owner.trim().isEmpty() ? getUser()
					.getEmail() : owner);
			return result;
		} finally {
			pm.close();
		}

	}

	/*
	 * Just a temp method to fix data. Only run this once.
	 */
	public void getConvertStagedUsers() throws IllegalArgumentException,
			NotLoggedInException {
		StudyWordsServiceImpl.checkLoggedIn();

		PersistenceManager pm = StudyWordsServiceImpl.getPersistenceManager();
		List<WordPair> wordPairs = new ArrayList<>();
		try {
			Query q = pm.newQuery(CardStaging.class);
			List<CardStaging> cards = (List<CardStaging>) q.execute();
			for (CardStaging card : cards) {
				card.setOwner(getUser().getEmail());
				card.setUser(null);
				pm.flush();
			}

		} finally {
			pm.close();
		}
	}

	@Override
	public void delete(String id) throws IllegalArgumentException,
			NotLoggedInException {
		PersistenceManager pm = StudyWordsServiceImpl.getPersistenceManager();
		try {
			Query q = pm.newQuery(CardStaging.class, "id == i");
			q.declareParameters("java.lang.String i");
			List<CardStaging> cards = (List<CardStaging>) q.execute(id);

			if (getUser().getEmail().equals(cards.get(0).getOwner())) {
				LOG.info("Deleted: " + q.deletePersistentAll(id) + " card.");
			} else {
				LOG.info("Cant delete card owned by someone else.");
				throw new IllegalArgumentException(
						"Cant delete card owned by someone else.");
			}

		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			LOG.info(e.getMessage());
			throw new IllegalArgumentException(
					"failed to delete card from staging");
		} finally {
			pm.flush();
			pm.close();
		}
	}

	@Override
	public void promote(String id) throws IllegalArgumentException,
			NotLoggedInException {
		StudyWordsServiceImpl.checkLoggedIn();

		PersistenceManager pm = StudyWordsServiceImpl.getPersistenceManager();
		List<WordPair> wordPairs = new ArrayList<>();

		try {
			Query q = pm.newQuery(CardStaging.class, "id == i");
			q.declareParameters("java.lang.String i");
			List<CardStaging> cards = (List<CardStaging>) q.execute(id);
			CardStaging card = cards.get(0);
			UUID uuid = java.util.UUID.randomUUID();

//			if (StudyWordsServiceImpl.exists(card.getWord(), getUser()) != null) {
//				throw new IllegalArgumentException(
//						"A card with this term already exists in the card stack. Promotion refused.");
//			}

			Card pair = new Card(uuid.toString(), card.getWord(),
					card.getDefinition(), getUser());
			pm.makePersistent(pair);
			if (getUser().getEmail().equals(card.getOwner())) {
				pm.deletePersistent(card);
			}
			pm.flush();

		} finally {
			pm.close();
		}

	}

	@Override
	public WordPair update(String id, String word, String definition)
			throws IllegalArgumentException, NotLoggedInException {

		StudyWordsServiceImpl.checkLoggedIn();

		PersistenceManager pm = StudyWordsServiceImpl.getPersistenceManager();

		CardStaging exists = exists(word, getUser());
		try {
			Query q = pm.newQuery(CardStaging.class, "id == i");
			q.declareParameters("java.lang.String i");
			List<CardStaging> cards = (List<CardStaging>) q.execute(id);
			CardStaging card = cards.get(0);

			if (exists != null && !card.getId().equals(exists.getId())) {
				throw new IllegalArgumentException(
						"A card with this term already exists in staging.");
			}
			if (getUser().getEmail().equals(cards.get(0).getOwner())) {
				card.setWord(word);
				card.setDefinition(definition);
			} else {
				LOG.info("Cant delete card owned by someone else.");
				throw new IllegalArgumentException(
						"Can't update card owned by someone else.");
			}
			pm.flush();
			return convert(card);

		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			LOG.info(e.getMessage());
			throw new IllegalArgumentException("Failed to delete");
		} finally {
			pm.close();
		}
	}

	public static void checkDoesTermExist(String word, User user) {
		if (existsPubished(word, user) != null) {
			throw new IllegalArgumentException(
					"A card with this term already exists in the card stack.");
		}

		if (exists(word, user) != null) {
			throw new IllegalArgumentException(
					"A card with this term already exists in staging");
		}
	}

	WordPair convert(CardStaging pair) {
		WordPair gwtPair;
		gwtPair = new WordPair(pair.getId(), pair.getWord(),
				pair.getDefinition());
		gwtPair.setUser(pair.getOwner());
		return gwtPair;
	}

	@Override
	public List<String> getStagingFriends() throws IllegalArgumentException,
			NotLoggedInException {
		StudyWordsServiceImpl.checkLoggedIn();

		PersistenceManager pm = StudyWordsServiceImpl.getPersistenceManager();
		List<String> firends = new ArrayList<>();
		try {
			// Query q = pm.newQuery(CardStaging.class, "user == u");
			// q.declareParameters("com.google.appengine.api.users.User u");
			// q.setOrdering("createDate");
			// List<CardStaging> cards = (List<CardStaging>)
			// q.execute(getUser());

			// //Until i figure out how to import as a user, the user field will
			// be null for imported ones.
			// Query q = pm.newQuery(CardStaging.class, );
			// q.setOrdering("createDate");
			// List<CardStaging> cards = (List<CardStaging>) q.execute();

			Query q = pm.newQuery(CardStaging.class);
			q.setResult("distinct owner");
			List<String> users = (List<String>) q.execute();

			for (String o : users) {
				firends.add(o);
			}

		} catch (Exception e) {
			LOG.log(Level.WARNING, e.getMessage());
		} finally {
			pm.close();
		}
		return firends;
	}

	public static CardStaging exists(String term, User user) {
		PersistenceManager pm = StudyWordsServiceImpl.getPersistenceManager();
		try {
			Query q = pm.newQuery(CardStaging.class, "owner == o && word == w");
			q.declareParameters("java.lang.String o, java.lang.String w");
			List<CardStaging> cards = (List<CardStaging>) q.execute(
					user.getEmail(), term);

			// Query q = pm.newQuery(CardStaging.class, "word == w");
			// q.declareParameters("java.lang.String w");
			// List<CardStaging> cards = (List<CardStaging>) q.execute(term);

			if (cards.isEmpty()) {
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

	public static Card existsPubished(String term, User user) {
		PersistenceManager pm = StudyWordsServiceImpl.getPersistenceManager();
		try {
			Query q = pm.newQuery(Card.class, "user == u && word == w");
			q.declareParameters("com.google.appengine.api.users.User u, java.lang.String w");
			List<Card> cards = (List<Card>) q.execute(user, term);
			// Query q = pm.newQuery(Card.class, "word == w");
			// q.declareParameters("java.lang.String w");
			// List<Card> cards = (List<Card>) q.execute(term);

			if (cards.isEmpty()) {
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

	/*
	 * Call only once. This is intended to migrate all of the data from Cards
	 * and CardStaging into the StudyItem/StudyItemMeta paradigm
	 */
	@Override
	public int migrateToStudyItemSchema() {
		PersistenceManager pm = StudyWordsServiceImpl.getPersistenceManager();
		List<String> addedWords = new ArrayList<>();
		int count = 0;
		try {
			{
				Query q = pm.newQuery(StudyItem.class);
				pm.deletePersistentAll((List<StudyItem>)q.execute());
			}
			
			{
				Query q = pm.newQuery(StudyItemMeta.class);
				pm.deletePersistentAll((List<StudyItemMeta>)q.execute());
			}
			
			Query q = pm.newQuery(Card.class);
			List<Card> cards = (List<Card>) q.execute();
			for (Card card : cards) {
				if (addedWords.contains(card.getWord())) {
					LOG.log(Level.WARNING, "Word: " + card.getWord()
							+ " already exists. Skipping.");
					continue;
				}

				if (card.getUser() == null) {
					LOG.log(Level.WARNING, "Word: " + card.getWord()
							+ " has no user. Skipping.");
					continue;
				}

				addedWords.add(card.getWord());

				StudyItem studyItem = new StudyItem();
				StudyItemMeta studyItemMeta = new StudyItemMeta();
				studyItem.setCreateDate(card.getCreateDate());
				studyItem.setOwner(card.getUser().getEmail());
				studyItem.setDefinition(card.getDefinition());
				studyItem.setId(card.getId());
				studyItem.setWord(card.getWord());

				studyItemMeta.setConfidence(card.getConfidence());
				studyItemMeta.setCreateDate(card.getCreateDate());
				studyItemMeta.setDifficulty(card.getDifficulty());
				studyItemMeta.setIncorrectCount(card.getIncorrectCount());
				studyItemMeta.setLastUpdate(card.getLastUpdate());
				studyItemMeta.setOwner(card.getUser().getEmail());
				studyItemMeta.setStudyItemId(card.getId());
				studyItemMeta.setViewCount(card.getViewCount());

				pm.makePersistent(studyItem);
				pm.makePersistent(studyItemMeta);
				count++;

				LOG.log(Level.WARNING, "Word: " + card.getWord()
						+ " created as StudyItem.");
			}

			Query q2 = pm.newQuery(CardStaging.class);
			List<CardStaging> cardsStaging = (List<CardStaging>) q2.execute();
			for (CardStaging card : cardsStaging) {
				if (addedWords.contains(card.getWord())) {
					LOG.log(Level.WARNING, "Staged word: " + card.getWord()
							+ " already exists. Skipping.");
					continue;
				}

				if (card.getOwner() == null) {
					LOG.log(Level.WARNING, "Staged word: " + card.getWord()
							+ " has no owner. Skipping.");
					continue;
				}

				addedWords.add(card.getWord());

				StudyItem studyItem = new StudyItem();
				studyItem.setCreateDate(card.getCreateDate());
				studyItem.setOwner(card.getOwner());
				studyItem.setDefinition(card.getDefinition());
				studyItem.setId(card.getId());
				studyItem.setWord(card.getWord());

				pm.makePersistent(studyItem);
				count++;
				LOG.log(Level.WARNING, "Staged Word: " + card.getWord()
						+ " created as StudyItem.");
			}

		} catch (Exception e) {
			LOG.log(Level.SEVERE, e.getMessage());
			throw new RuntimeException(e.getMessage());
		} finally {
			pm.close();
		}

		return count;

	}

}
