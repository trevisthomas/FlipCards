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

import org.ttdc.flipcards.client.StudyWordsService;
import org.ttdc.flipcards.server.sql.SqlMigrator;
import org.ttdc.flipcards.shared.AutoCompleteWordPairList;
import org.ttdc.flipcards.shared.CardOrder;
import org.ttdc.flipcards.shared.ItemFilter;
import org.ttdc.flipcards.shared.NotLoggedInException;
import org.ttdc.flipcards.shared.PagedWordPair;
import org.ttdc.flipcards.shared.QuizOptions;
import org.ttdc.flipcards.shared.Tag;
import org.ttdc.flipcards.shared.WordPair;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

public class StudyWordsServiceImpl extends RemoteServiceServlet implements
		StudyWordsService {
	private static final Logger LOG = Logger
			.getLogger(StudyWordsServiceImpl.class.getName());
	private static final PersistenceManagerFactory PMF = JDOHelper
			.getPersistenceManagerFactory("transactions-optional");

	Cache cache;

	public StudyWordsServiceImpl() {
		Map props = new HashMap();
		props.put(GCacheFactory.EXPIRATION_DELTA, 3600);
		try {
			CacheFactory cacheFactory = CacheManager.getInstance()
					.getCacheFactory();
			cache = cacheFactory.createCache(props);
		} catch (CacheException e) {
			logException(e);
		}
	}

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
		if (currentUser != null) {
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
	public WordPair addWordPair(String word, String definition, List<String> tagIds)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		UUID uuid = java.util.UUID.randomUUID();
		if (exists(word) != null) {
			throw new IllegalArgumentException(
					"Coudld not be added.  A card with this term already exists.");
		}

		PersistenceManager pm = getPersistenceManager();

		try {
			StudyItem studyItem = new StudyItem();
			studyItem.setId(uuid.toString());
			studyItem.setWord(word);
			studyItem.setDefinition(definition);
			studyItem.setOwner(getUser().getEmail());

			pm.makePersistent(studyItem);
			pm.flush();
			return convert(studyItem, null);
		} catch (Exception e) {
			LOG.log(Level.WARNING, e.getMessage());
			return null;
		} finally {
			pm.close();
		}
	}

	@Override
	public WordPair setActiveStatus(String id, boolean active)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		try {
			StudyItem studyItem = findStudyItem(id);
			StudyItemMeta studyItemMeta = null;
			if (active) {
				studyItemMeta = new StudyItemMeta();
				studyItemMeta.setStudyItemId(studyItem.getId());
				studyItemMeta.setOwner(getUser().getEmail());
				pm.makePersistent(studyItemMeta);
			} else {
				Query q = pm.newQuery(StudyItemMeta.class,
						"studyItemId == i && owner == o");
				q.declareParameters("java.lang.String i, java.lang.String o");
				List<StudyItemMeta> cards = (List<StudyItemMeta>) q.execute(
						studyItem.getId(), getUser().getEmail());
				studyItemMeta = cards.get(0);
				pm.deletePersistent(studyItemMeta);
				studyItemMeta = null;
			}
			pm.flush();
			return convert(studyItem, studyItemMeta);
		} catch (Exception e) {
			LOG.log(Level.WARNING, e.getMessage());
			return null;
		} finally {
			pm.close();
		}
	}

	@Override
	public void assignSelfToUserlessWords() throws NotLoggedInException {
		// checkLoggedIn();
		// PersistenceManager pm = getPersistenceManager();
		// Transaction trans = pm.currentTransaction();
		// try {
		// Query q = pm.newQuery(Card.class);
		// List<Card> result = (List<Card>) q.execute();
		//
		// for (Card pair : result) {
		// // If the user is null, fix it!
		// if (pair.getUser() == null) {
		// /*
		// * This is a hack because i cant figure out how to get the
		// * logged in user from the upload servlet so if i see those
		// * i fix them here.
		// */
		// trans.begin();
		// pair.setUser(getUser());
		// trans.commit();
		// }
		// }
		//
		// } catch (Exception e) {
		// LOG.log(Level.WARNING, e.getMessage());
		// trans.rollback();
		// } finally {
		// pm.close();
		// }
		throw new RuntimeException("Deprecated");
	}

	WordPair convert(StudyItem studyItem, StudyItemMeta studyItemMeta)
			throws NotLoggedInException {

		WordPair gwtPair;

		gwtPair = new WordPair(studyItem.getId(), studyItem.getWord(),
				studyItem.getDefinition(), "");

		if (studyItemMeta != null) {
			gwtPair.setCorrectCount(studyItemMeta.getViewCount()
					- studyItemMeta.getIncorrectCount());
			gwtPair.setTestedCount(studyItemMeta.getViewCount());
			gwtPair.setDifficulty(studyItemMeta.getDifficulty());
			gwtPair.setAverageTime(studyItemMeta.getAverageTime());
			if(studyItemMeta.getLastUpdate() != null){
				gwtPair.setLastUpdate(new Date(studyItemMeta.getLastUpdate().getTime()));
			} else {
				gwtPair.setLastUpdate(new Date());
			}
			gwtPair.setActive(true);
		}

		Map<String, Tag> allTags = getAllTagsMap();
		List<TagAssociation> tagAssociations = getTagAssociations(studyItem
				.getId());

		for (TagAssociation tagAss : tagAssociations) {
			Tag tag = allTags.get(tagAss.getTagId());
			if (tag != null) { // This happened in dev after mucking with the db
								// schema. I have a Tag with a null tagId in the
								// ass db.
				gwtPair.getTags().add(tag);
			}
		}

		gwtPair.setUser(studyItem.getOwner());
		if (getUser().getEmail().equals(studyItem.getOwner())) {
			gwtPair.setDeleteAllowed(true);
		}
		return gwtPair;
	}

	// WordPair convert(Card pair) throws NotLoggedInException{
	// // WordPair gwtPair;
	// // if (switchEm) {
	// // gwtPair = new WordPair(pair.getId(), pair.getDefinition(),
	// // pair.getWord());
	// // } else {
	// // gwtPair = new WordPair(pair.getId(), pair.getWord(),
	// // pair.getDefinition());
	// // }
	// // if(pair.getUser() != null){
	// // gwtPair.setUser(pair.getUser().getNickname());
	// // }
	// // return gwtPair;
	// return convert(pair, false);
	//
	// }

	// @Override
	// public List<WordPair> getAllWordPairs() throws NotLoggedInException {
	// checkLoggedIn();
	// String owner = getUser().getEmail(); //Just getting words that the user
	// owns, he may have active owrds that he doesnt own. so fix that!
	//
	// return getWordPairAll(owner);
	// }

	@Override
	public List<WordPair> getWordPairsForPage(int pageNumber)
			throws IllegalArgumentException, NotLoggedInException {
//		checkLoggedIn();
//		List<WordPair> pageOfPairs = (List<WordPair>) cache
//				.get(new CacheKeyPageination(getUser().getEmail(), pageNumber));
//		if (pageOfPairs == null) {
//			throw new IllegalArgumentException(
//					"Paginated data is stale.  Refresh the page.");
//		}
//		return pageOfPairs;
		throw new IllegalArgumentException("Not used right?");
	}

	@Override
	public PagedWordPair getWordPairs(List<String> tagIds, List<String> users,
			ItemFilter filter, CardOrder cardOrder, int pageNumber, int perPage)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		// loadMetaDataToCache(); //Brute force. Just build the cache every
		// time.

		
		
		if (users.isEmpty()) {
			users = getStudyFriends();

			// Defaulting to self until i get the settings to save.
			// users = new ArrayList<>();
			// users.add(getUser().getEmail());
		}

		
		
		List<WordPair> wordPairs = new ArrayList<>();

		// for(String owner : users){
		// if(tagIds.isEmpty()){
		// switch (filter) {
		// case ACTIVE:
		// wordPairs.addAll(getWordPairsActive(owner));
		// break;
		// case INACTIVE:
		// wordPairs.addAll(getWordPairInactive(owner));
		// break;
		// case BOTH:
		// default:
		// wordPairs.addAll(getWordPairAll(owner));
		// break;
		// }
		//
		// }
		// else{
		// wordPairs.addAll(getCards(tagIds, owner, filter));
		// }
		// }
		PagedWordPair pwp = null;

		if (tagIds.isEmpty()) {
			switch (filter) {
			case ACTIVE:
				pwp = getWordPairsActive(users, pageNumber, perPage);
				break;
			case INACTIVE:
				// throw new
				// IllegalArgumentException("INACTIVE filter not yet implemented");
				pwp = getWordPairInactive(users, pageNumber, perPage);
				break;
			case BOTH:
			default:
				pwp = getWordPairAll(users, pageNumber, perPage);
				break;
			}

		} else {
			pwp = getWordPairsWithTags(tagIds, users, filter, pageNumber, perPage);
			// wordPairs.addAll(getCards(tagIds, owner, filter));
//			throw new IllegalArgumentException("Tag filter not yet implemented");
		}

		// int i = 1;
		// for (WordPair wp : wordPairs) {
		// wp.setDisplayOrder(i++);
		// }

		// Object firstPageTest = cache.get(new
		// CacheKeyPageination(getUser().getEmail(), 1));

		// PagedWordPair pwp = cachePaginatedWordPairs(wordPairs, perPage);
		return pwp;

		// return wordPairs;
	}

	// private PagedWordPair cachePaginatedWordPairs(List<WordPair> wordPairs,
	// int perPage) {
	// PagedWordPair pwp = new PagedWordPair();
	// int pageCount = 0;
	// int toIndex;
	// pwp.setTotalCardCount(wordPairs.size());
	// // for(WordPair wp : wordPairs){
	// while(true){
	// pageCount++;
	// toIndex = pageCount * perPage;
	// if(toIndex > wordPairs.size()){
	// toIndex = wordPairs.size();
	// }
	// List<WordPair> page = new ArrayList<>(wordPairs.subList((pageCount - 1) *
	// perPage, toIndex));
	// cache.put(new CacheKeyPageination(getUser().getEmail(), pageCount),
	// page);
	// if(pwp.getWordPair() == null){
	// pwp.setWordPair(page); //First page
	// }
	//
	// if(toIndex == wordPairs.size()){
	// pwp.setPageCount(pageCount);
	// break;
	// }
	// }
	// return pwp;
	// }

	// private List<WordPair> getWordPairAll(String owner)
	// throws NotLoggedInException {
	// PersistenceManager pm = getPersistenceManager();
	// List<WordPair> wordPairs = new ArrayList<>();
	// try {
	// Query q = pm.newQuery(StudyItem.class, "owner == o");
	// q.declareParameters("java.lang.String o");
	// q.setOrdering("createDate desc");
	// List<StudyItem> cards = (List<StudyItem>) q.execute(owner);
	// int i = 1;
	// for (StudyItem card : cards) {
	// WordPair pair = convert(card, findMetaData(card.getId(), owner));
	// pair.setDisplayOrder(i++);
	// wordPairs.add(pair);
	// }
	//
	// } catch (Exception e) {
	// logException(e);
	// } finally {
	// pm.close();
	// }
	// return wordPairs;
	// }
	//
	/**
	 * Ok lets do it for real.
	 * 
	 * @param owners
	 * @return
	 * @throws NotLoggedInException
	 */
	private PagedWordPair getWordPairAll(List<String> owners, int pageNumber,
			int pageSize) throws NotLoggedInException {
		PersistenceManager pm = getPersistenceManager();
		PagedWordPair pagedWordPair = new PagedWordPair();
		List<WordPair> wordPairs = new ArrayList<>();

		// Fancy fucking cursor bullshit doesn't work when you use contains.
		try {
			Query q = pm.newQuery(StudyItem.class, ":p.contains(owner)");
			if (pageNumber == 1) {
				q.setResult("count(this)");
				Long count = (Long) q.execute(owners);
				pagedWordPair.setTotalCardCount(count);
				q.setResult(null);
			}
			q.setOrdering("createDate desc");
			int start = (pageNumber - 1) * pageSize;
			int end = start + pageSize;
			q.setRange(start, end);
			List<StudyItem> cards = (List<StudyItem>) q.execute(owners);

			for (StudyItem card : cards) {
				WordPair pair = convert(card,
						findMetaData(card.getId(), card.getOwner()));
				wordPairs.add(pair);
			}

			pagedWordPair.setWordPair(wordPairs);

		} catch (Exception e) {
			logException(e);
		} finally {
			pm.close();
		}
		return pagedWordPair;
	}
	
	@Override
	public WordPair getStudyItem(String studyItemId) throws IllegalArgumentException, NotLoggedInException{
		StudyItem si = findStudyItem(studyItemId);
		return convert(si, findMetaData(si.getId(), getUser().getEmail()));
	}
	
	@Override
	public AutoCompleteWordPairList getAutoCompleteWordPairs(List<String> owners, int sequence, String qstr) 
			throws IllegalArgumentException, NotLoggedInException{
		PersistenceManager pm = getPersistenceManager();
		
		if (owners.isEmpty()) {
			owners = getStudyFriends();
		}
		
		List<WordPair> wordPairs = new ArrayList<>();
		try {
			Query q = pm.newQuery(StudyItem.class, "word.startsWith(qstr) && owner == o");
			q.declareParameters("java.lang.String qstr, java.lang.String o");
			q.setOrdering("word desc");
			q.setRange(0, 10);
			List<StudyItem> cards = (List<StudyItem>) q.execute(qstr, owners);
			for (StudyItem card : cards) {
//				WordPair pair = convert(card,
//						findMetaData(card.getId(), card.getOwner()));
				WordPair pair = new WordPair(card.getId(), card.getWord(), card.getDefinition(), "");  //I think that this class is deprecated
				wordPairs.add(pair);
			}

		} catch (Exception e) {
			logException(e);
		} finally {
			pm.close();
		}
		return new AutoCompleteWordPairList(sequence, wordPairs);
	}

	private PagedWordPair getWordPairInactive(List<String> owners,
			int pageNumber, int pageSize) throws NotLoggedInException {
		PersistenceManager pm = getPersistenceManager();
		PagedWordPair pagedWordPair = new PagedWordPair();
		List<WordPair> wordPairs = new ArrayList<>();

		// Fancy fucking cursor bullshit doesn't work when you use contains.
		try {
//			Query q = pm.newQuery(StudyItem.class, ":p.contains(id)");

			// Get inactive id's
			List<String> inactiveIds = getInactiveIds(owners);
			pagedWordPair.setTotalCardCount(inactiveIds.size());

//			q.setOrdering("createDate desc");
			int start = (pageNumber - 1) * pageSize;
			int end = start + pageSize;

			// if(end > inactiveIds.size()){
			// end = inactiveIds.size();
			// }
			// q.setRange(start, end);
			// List<StudyItem> cards = (List<StudyItem>)
			// q.execute(inactiveIds.subList(start, start+4));

			for (String id : inactiveIds.subList(start,
					end > inactiveIds.size() ? inactiveIds.size() : end)) {
				StudyItem studyItem = findStudyItem(id);
				WordPair pair = convert(studyItem, null);
				wordPairs.add(pair);
			}

			pagedWordPair.setWordPair(wordPairs);

		} catch (Exception e) {
			logException(e);
		} finally {
			pm.close();
		}
		return pagedWordPair;
	}

	private List<String> getInactiveIds(List<String> owners) {
		PersistenceManager pm = getPersistenceManager();
		Query q = pm.newQuery(StudyItemMeta.class, "owner == o");
		q.declareParameters("java.lang.String o");
		q.setResult("studyItemId");
		List<String> myActiveCardIs = (List<String>) q.execute(getUser()
				.getEmail());

		Query q2 = pm.newQuery(StudyItem.class, "owner == o");
		q2.declareParameters("java.lang.String o");
		q2.setResult("id, createDate");
		q2.setOrdering("createDate desc"); //This is how we sort inactives!
//		List<String> allOfMyFriendsCardIds = (List<String>) q2.execute(owners);
		List<String> inactive = new ArrayList<>();
//		for (String id : allOfMyFriendsCardIds) {
//			if (!myActiveCardIs.contains(id)) {
//				inactive.add(id);
//			}
//		}
		List<Object[]> allOfMyFriendsCardIds = (List<Object[]>)q2.execute(owners);

		for (Object[] o : allOfMyFriendsCardIds) {
			String id = (String)o[0];
			if (!myActiveCardIs.contains(id)) {
				inactive.add(id);
			}
		}

		return inactive;

	}

	// private List<WordPair> getWordPairInactive(String owner)
	// throws NotLoggedInException {
	// PersistenceManager pm = getPersistenceManager();
	// List<WordPair> wordPairs = new ArrayList<>();
	// try {
	// Query q = pm.newQuery(StudyItem.class, "owner == o");
	// q.declareParameters("java.lang.String o");
	// q.setOrdering("createDate desc");
	// List<StudyItem> cards = (List<StudyItem>) q.execute(owner);
	// int i = 1;
	// for (StudyItem card : cards) {
	// StudyItemMeta meta = findMetaData(card.getId(), owner);
	// if(meta != null){
	// continue;
	// }
	// WordPair pair = convert(card, null);
	// pair.setDisplayOrder(i++);
	// wordPairs.add(pair);
	// }
	//
	// } catch (Exception e) {
	// logException(e);
	// } finally {
	// pm.close();
	// }
	// return wordPairs;
	// }
	//
	// /**
	// * This was a test to see if searching for a list of owners worked, it
	// does.
	// * @param owners
	// * @return
	// * @throws NotLoggedInException
	// */
	// private List<WordPair> getWordPairInactive(List<String> owners)
	// throws NotLoggedInException {
	// PersistenceManager pm = getPersistenceManager();
	// List<WordPair> wordPairs = new ArrayList<>();
	// try {
	// Query q = pm.newQuery(StudyItem.class, ":p.contains(owner)");
	// q.setOrdering("createDate desc");
	// List<StudyItem> cards = (List<StudyItem>) q.execute(owners);
	// int i = 1;
	// for (StudyItem card : cards) {
	// StudyItemMeta meta = findMetaData(card.getId(), card.getOwner());
	// if(meta != null){
	// continue;
	// }
	// WordPair pair = convert(card, null);
	// pair.setDisplayOrder(i++);
	// wordPairs.add(pair);
	// }
	//
	// } catch (Exception e) {
	// logException(e);
	// } finally {
	// pm.close();
	// }
	// return wordPairs;
	// }

	StudyItemMeta findMetaData(String studyItemId, String owner) {
		PersistenceManager pm = getPersistenceManager();
		Query q = pm.newQuery(StudyItemMeta.class,
				"studyItemId == i && owner == o");
		q.declareParameters("java.lang.String i, java.lang.String o");
		List<StudyItemMeta> cards = (List<StudyItemMeta>) q.execute(
				studyItemId, owner);
		if (cards.size() > 0) {
			return cards.get(0); // If the word isnt active, the expectation is
									// that this will return null.
		} else {
			return null;
		}
	}

	private StudyItemMeta findMetaDataCached(String studyItemId, String owner) {
		StudyItemMeta meta = (StudyItemMeta) cache
				.get(new CacheKeyStudyItemMeta(studyItemId, owner));
		return meta;
		// Makes no effort to really load it on a cache miss.

		// Query q = pm.newQuery(StudyItemMeta.class,
		// "studyItemId == i && owner == o");
		// q.declareParameters("java.lang.String i, java.lang.String o");
		// List<StudyItemMeta> cards = (List<StudyItemMeta>)
		// q.execute(studyItemId, owner);
		// if(cards.size() > 0){
		// return cards.get(0); //If the word isnt active, the expectation is
		// that this will return null.
		// }
		// else {
		// return null;
		// }
	}

	// /**
	// *
	// * The idea is, all of the meta data should be in the cache, and should
	// all be the same age?
	// */
	// boolean refreshCacheIfNeeded(){
	// PersistenceManager pm = getPersistenceManager();
	// Query q = pm.newQuery(StudyItemMeta.class);
	// q.setRange(0, 1);
	// List<StudyItemMeta> metaList = (List<StudyItemMeta>) q.execute();
	// if(metaList.size() > 1){
	// StudyItemMeta meta = metaList.get(0);
	// if(cache.get(meta) == null){
	// loadMetaDataToCache();
	// return true;
	// }
	// }
	// return false;
	// }

//	void loadMetaDataToCache() {
//		try {
//			PersistenceManager pm = getPersistenceManager();
//			Query q = pm.newQuery(StudyItemMeta.class);
//			List<StudyItemMeta> metaList = (List<StudyItemMeta>) q.execute();
//
//			for (StudyItemMeta meta : metaList) {
//				cache.put(
//						new CacheKeyStudyItemMeta(meta.getStudyItemId(), meta
//								.getOwner()), meta);
//			}
//		} catch (Exception e) {
//			logException(e);
//			throw e;
//		}
//	}
	


	// Hm quiz uses this one. Should probably rewrite to make it faster.
	private List<WordPair> getWordPairsActive(String owner)
			throws NotLoggedInException {
		PersistenceManager pm = getPersistenceManager();
		List<WordPair> list = new ArrayList<>();
		Query q = pm.newQuery(StudyItemMeta.class, "owner == o");
		q.declareParameters("java.lang.String o");
		q.setOrdering("createDate desc");
		List<StudyItemMeta> cards = (List<StudyItemMeta>) q.execute(owner);
		for (StudyItemMeta studyItemMeta : cards) {
			StudyItem studyItem = findStudyItem(studyItemMeta.getStudyItemId());
			// If study item is null then something really bad is wrong with the
			// data.
			if (studyItem == null) {
				LOG.severe("Study Item not found for metadata! id:"
						+ studyItemMeta.getStudyItemId());
				continue;
			}
			list.add(convert(studyItem, studyItemMeta));
		}
		return list;
	}
	
	

	private PagedWordPair getWordPairsActive(List<String> owners,
			int pageNumber, int pageSize) throws NotLoggedInException {
		PersistenceManager pm = getPersistenceManager();
		PagedWordPair pagedWordPair = new PagedWordPair();
		List<WordPair> wordPairs = new ArrayList<>();

		// Fancy fucking cursor bullshit doesn't work when you use contains.
		try {
			Query q = pm.newQuery(StudyItemMeta.class, ":p.contains(owner)");
			if (pageNumber == 1) {
				q.setResult("count(this)");
				Long count = (Long) q.execute(getUser().getEmail());
				pagedWordPair.setTotalCardCount(count);
				q.setResult(null);
			}
			q.setOrdering("createDate desc");
			/*
			 * Since you cant do joins, would it just make sense to put the word
			 * into the Meta so that i can sort on it?
			 */

			int start = (pageNumber - 1) * pageSize;
			int end = start + pageSize;
			q.setRange(start, end);
			List<StudyItemMeta> studyItemMetaList = (List<StudyItemMeta>) q
					.execute(getUser().getEmail());

			for (StudyItemMeta studyItemMeta : studyItemMetaList) {
				StudyItem studyItem = findStudyItem(studyItemMeta
						.getStudyItemId());
				if (studyItem == null) {
					LOG.severe("Study Item not found for metadata! id:"
							+ studyItemMeta.getStudyItemId());
					continue;
				}
				wordPairs.add(convert(studyItem, studyItemMeta));
			}

			pagedWordPair.setWordPair(wordPairs);

		} catch (Exception e) {
			logException(e);
		} finally {
			pm.close();
		}
		return pagedWordPair;
	}

	private StudyItem findStudyItem(String studyItemId) {
		PersistenceManager pm = getPersistenceManager();
		Query q = pm.newQuery(StudyItem.class, "id == i");
		q.declareParameters("java.lang.String i");
		List<StudyItem> list = (List<StudyItem>) q.execute(studyItemId);
		return list.get(0);
	}

	@Override
	public List<String> getStudyFriends() throws IllegalArgumentException,
			NotLoggedInException {
		StudyWordsServiceImpl.checkLoggedIn();

		PersistenceManager pm = StudyWordsServiceImpl.getPersistenceManager();
		List<String> friends = new ArrayList<>();
		friends.add(getUser().getEmail()); // Self first
		try {
			Query q = pm.newQuery(StudyItem.class);
			q.setResult("distinct owner");
			List<String> users = (List<String>) q.execute();
			// This is a hack to just make every user your friend
			for (String o : users) {
				if (!o.equals(getUser().getEmail())) {
					friends.add(o);
				}
			}

		} catch (Exception e) {
			LOG.log(Level.WARNING, e.getMessage());
		} finally {
			pm.close();
		}
		return friends;
	}

//	private List<WordPair> getCards(List<String> tagIds, String owner,
//			ItemFilter filter) throws NotLoggedInException {
//		Set<WordPair> cardSet = new HashSet<>();
//		for (String tagId : tagIds) {
//			List<WordPair> list = getCards(tagId, owner, filter);
//			cardSet.addAll(list); // Probably should de dup. Ok the set should
//									// de dup
//		}
//		return new ArrayList<>(cardSet);
//	}


	
	public PagedWordPair getWordPairsWithTags(List<String> tagIds, List<String> owners, ItemFilter filter, int pageNumber, int pageSize)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();

		PersistenceManager pm = getPersistenceManager();
		PagedWordPair pagedWordPairs = new PagedWordPair();
		List<WordPair> wordPairs = new ArrayList<>();
		try {
			// Query q = pm.newQuery(TagAssociation.class,
			// "user == u && tagId == tid");
			// q.declareParameters("com.google.appengine.api.users.User u, java.lang.String tid");
			// List<TagAssociation> tagAsses = (List<TagAssociation>)
			// q.execute(getUser(), tagId);
			Query q = pm.newQuery(TagAssociation.class, "tagId == tid && cardOwner == owner");
			q.declareParameters("java.lang.String tid, java.lang.String owner");
			

			//Ignoring ItemFilter for the time being.
			
//			Query q = pm.newQuery(StudyItem.class, ":p.contains(owner)");
			if (pageNumber == 1) {
				q.setResult("count(this)");
				Long count = (Long) q.execute(tagIds, owners);
				pagedWordPairs.setTotalCardCount(count);
				q.setResult(null);
			}
			
			int start = (pageNumber - 1) * pageSize;
			int end = start + pageSize;
			q.setRange(start, end);
			List<TagAssociation> tagAsses = (List<TagAssociation>) q
					.execute(tagIds, owners);

			for (TagAssociation tagAss : tagAsses) {
				WordPair pair = convert(findStudyItem(tagAss.getCardId()),
						findMetaData(tagAss.getCardId(), getUser().getEmail()));
				wordPairs.add(pair);
			}

			pagedWordPairs.setWordPair(wordPairs);
			
			

//			boolean addIt = false;
//			for (TagAssociation ta : tagAsses) {
//				Query q2 = pm
//						.newQuery(StudyItem.class, "id == i && owner == o");
//				q2.declareParameters("java.lang.String i, java.lang.String o");
//				List<StudyItem> studyItems = (List<StudyItem>) q2.execute(
//						ta.getCardId(), owner);
//				if (studyItems.size() > 0) { // Should be 1, but who cares
//												// right?
//					StudyItem studyItem = studyItems.get(0);
//					StudyItemMeta studyItemMeta = findMetaData(
//							studyItem.getId(), owner);
//					addIt = false;
//					switch (filter) {
//					case ACTIVE:
//						if (studyItemMeta != null) {
//							addIt = true;
//						}
//						break;
//					case INACTIVE:
//						if (studyItemMeta == null) {
//							addIt = true;
//						}
//						break;
//					case BOTH:
//					default:
//						addIt = true;
//						break;
//					}
//					if (addIt) {
//						wordPairs.add(convert(studyItem, studyItemMeta));
//					}
//				}
//			}

			// Query q2 = pm.newQuery(StudyItem.class, ":p.contains(id)");
			// List<StudyItem> cards = (List<StudyItem>) q2.execute(cardIds);
			// for(StudyItem studyItem : cards){
			// wordPairs.add(convert(studyItem, findMetaData(studyItem.getId(),
			// getUser().getEmail())));
			// }

			return pagedWordPairs;

		} catch (Exception e) {
			logException(e);
			throw (e);
		} finally {
			pm.close();
		}
	}

	@Override
	public WordPair updateWordPair(String id, String word, String definition, String example)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		Transaction trans = pm.currentTransaction();

		StudyItem exists = exists(word);
		// Does it exist, and is not me!
		if (exists != null && !exists.getId().equals(id)) {
			throw new IllegalArgumentException("Already exists");
		}

		try {
			Query q = pm.newQuery(StudyItem.class, "id == i");
			q.declareParameters("java.lang.String i");
			List<StudyItem> cards = (List<StudyItem>) q.execute(id);
			trans.begin();
			StudyItem studyItem = cards.get(0);
			studyItem.setWord(word);
			studyItem.setDefinition(definition);
			trans.commit();

			return convert(studyItem,
					findMetaData(studyItem.getId(), getUser().getEmail()));
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

		// Probably shouldnt allow deletion of words that are associated... or
		// maybe words that you dont own? This doesnt check for any of that.
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		Transaction trans = pm.currentTransaction();
		long deleteCount = 0;
		try {
			Query q = pm.newQuery(StudyItem.class, "id == identity");
			q.declareParameters("java.lang.String identity");
			List<StudyItem> wordPairs = (List<StudyItem>) q.execute(id);
			trans.begin();
			for (StudyItem pair : wordPairs) {
				if (id.equals(pair.getId())) {
					pm.deletePersistent(pair);
					deleteCount++;
				}
			}

			Query q2 = pm.newQuery(TagAssociation.class, "cardId == cid");
			q2.declareParameters("java.lang.String cid");
			LOG.info("Also deleted " + q2.deletePersistentAll(id)
					+ " tag associations.");

			Query q3 = pm.newQuery(StudyItemMeta.class, "studyItemId == cid");
			q3.declareParameters("java.lang.String cid");
			LOG.info("Also deleted " + q3.deletePersistentAll(id)
					+ " meta data instances.");

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

	public static StudyItem exists(String term) {
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(StudyItem.class, "word == w");
			q.declareParameters("java.lang.String w");

			List<StudyItem> studyItems = (List<StudyItem>) q.execute(term);

			if (studyItems.size() == 0) {
				return null;
			}

			return studyItems.get(0);
		} catch (Exception e) {
			LOG.log(Level.WARNING, e.getMessage());
		} finally {
			pm.close();
		}
		return null;
	}

	@Override
	public void answerQuestion(String id, long duration, Boolean correct)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		Transaction trans = pm.currentTransaction();
		try {
			trans.begin();
			Query q = pm.newQuery(StudyItemMeta.class, "studyItemId == identity && owner == o");
			q.declareParameters("java.lang.String identity, java.lang.String o");
			List<StudyItemMeta> quizStats = (List<StudyItemMeta>) q.execute(id, getUser().getEmail());

			if (quizStats.size() != 1) {
				throw new RuntimeException("Failed to update score.");
			}

			// Query for the word, and then put the dictionary id into the
			// UserStat. You need it there for the filtering to work

			StudyItemMeta studyItemMeta = quizStats.get(0);
			if (!correct) {
				studyItemMeta.setIncorrectCount(studyItemMeta
						.getIncorrectCount() + 1);
			}
			studyItemMeta.setLastUpdate(new Date());
			studyItemMeta.setViewCount(studyItemMeta.getViewCount() + 1);
			studyItemMeta.setTimedViewCount(studyItemMeta.getTimedViewCount() + 1); //This was added so that cards that existed before timing mattered will have accurate averages.  It would also allow you to have non-timed runs, but i'm not implementing that now.
			
			studyItemMeta.setDifficulty((double) studyItemMeta
					.getIncorrectCount()
					/ (double) studyItemMeta.getViewCount());
			
			studyItemMeta.setTotalTime(studyItemMeta.getTotalTime() + duration);
			studyItemMeta.setAverageTime(studyItemMeta.getTotalTime() / studyItemMeta.getTimedViewCount());
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
			logException(e);
			throw new IllegalArgumentException("Failed to update score!");
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
		try {
			wordPairs = getWordPairsActive(getUser().getEmail());
			if (!quizOptions.getTagIds().isEmpty()) {
				wordPairs = applyTagFilter(wordPairs, quizOptions.getTagIds());
			}

			switch (quizOptions.getCardOrder()) {
			case SLOWEST:
				Collections.sort(wordPairs, new StudyItemMeta.SortAverageTimeDesc());
				break;
			case EASIEST:
				Collections.sort(wordPairs, new StudyItemMeta.SortDifficulty());
				break;
			case HARDEST:
				Collections.sort(wordPairs,
						new StudyItemMeta.SortDifficultyDesc());
				break;
			case LATEST_ADDED:
				Collections.sort(wordPairs,
						new StudyItemMeta.SortCreateDateDesc());
				break;
			case LEAST_STUDIED:
				Collections.sort(wordPairs, new StudyItemMeta.SortStudyCount());
				break;
			case LEAST_RECIENTLY_STUDIED:
				Collections.sort(wordPairs, new StudyItemMeta.SortStudyDate());
				break;
			case RANDOM:
				Collections.shuffle(wordPairs);
				break;
			}

			if (quizOptions.getSize() > 0
					&& wordPairs.size() > quizOptions.getSize()) {
				return new ArrayList<>(wordPairs.subList(0,
						quizOptions.getSize()));
			} else {
				return wordPairs;
			}

		} catch (Exception e) {
			logException(e);
			throw new RuntimeException("Internal server error.");
		}

		finally {
			pm.close();
		}
	}

	private List<WordPair> applyTagFilter(List<WordPair> wordPairs,
			List<String> tagIds) {
		List<WordPair> filtered = new ArrayList<>();
		for (WordPair wp : wordPairs) {
			for (Tag t : wp.getTags()) {
				if (tagIds.contains(t.getTagId())) {
					filtered.add(wp);
					break;
				}
			}
		}
		return filtered;
	}

	private void logException(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		LOG.severe(sw.toString());
//		LOG.severe(e.toString());
	}

	@Override
	public void applyTag(String tagId, String studyItemId)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		
		try {
			StudyItem studyItem = findStudyItem(studyItemId);
			//Remember, this is the card owner.  Not tue person creating the tag! This was done to make filter query easier
			TagAssociation tag = new TagAssociation(studyItem.getOwner(), tagId, studyItemId);
			pm.makePersistent(tag);
		} catch (Exception e) {
			logException(e);
			throw new IllegalArgumentException("Failed to add tag");
		} finally {
			pm.close();
		}
	}

	@Override
	public Tag createTag(String name) throws IllegalArgumentException,
			NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		try {
			UUID uuid = java.util.UUID.randomUUID();
			PersistantTagName tagName = new PersistantTagName(getUser(),
					uuid.toString(), name);
			pm.makePersistent(tagName);
			return new Tag(tagName.getTagId(), tagName.getTagName());
		} catch (Exception e) {
			logException(e);
			throw new IllegalArgumentException("Failed to add tag");
		} finally {
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
			List<PersistantTagName> serverTags = (List<PersistantTagName>) q
					.execute(tagId);
			PersistantTagName tagName = serverTags.get(0);
			if (tagName != null) {
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
		try {
			Query q = pm.newQuery(TagAssociation.class, "tagId == i");
			q.declareParameters("java.lang.String i");

			Query q2 = pm.newQuery(PersistantTagName.class, "tagId == i");
			q2.declareParameters("java.lang.String i");

			trans.begin();

			LOG.info("Deleted: " + q.deletePersistentAll(tagId) + " tags...");
			LOG.info("Deleted: " + q2.deletePersistentAll(tagId) + " tagNames");

			trans.commit();
		} catch (Exception e) {
			trans.rollback();
			LOG.info(e.getMessage());
			throw new IllegalArgumentException("faild tag opperation");
		} finally {
			pm.flush();
			pm.close();
		}
	}

	@Override
	public void deTag(String tagId, String cardId)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(TagAssociation.class,
					"tagId == tid && cardId == cid");
			q.declareParameters("java.lang.String tid, java.lang.String cid");
			long deleted = q.deletePersistentAll(tagId, cardId);
			LOG.info("Detaged: " + deleted + " items");
		} catch (Exception e) {
			LOG.info(e.getMessage());
			throw new IllegalArgumentException("Failed to remove tag");
		} finally {
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
			NotLoggedInException {
		Map<String, Tag> map = new HashMap<>();
		List<Tag> tags = getAllTagNames();
		for (Tag tag : tags) {
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
			List<PersistantTagName> serverTagNames = (List<PersistantTagName>) q
					.execute();

			for (PersistantTagName serverTagName : serverTagNames) {
				tags.add(new Tag(serverTagName.getTagId(), serverTagName
						.getTagName()));
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

	public List<TagAssociation> getTagAssociations(String cardId)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();

		PersistenceManager pm = getPersistenceManager();
		List<TagAssociation> tagAsses = new ArrayList<>();

		try {

			Query q = pm.newQuery(TagAssociation.class, "cardId == cid");
			q.declareParameters("java.lang.String cid");
			tagAsses = (List<TagAssociation>) q.execute(cardId);

		} catch (Exception e) {
			LOG.info(e.getMessage());
			throw new IllegalArgumentException(
					"Failed to retrieve tag associations");
		} finally {
			pm.close();
		}
		return tagAsses;
	}
	
	@Override
	public boolean migrate(int table, int pageNumber) throws NotLoggedInException{
		checkLoggedIn();
		return SqlMigrator.migrate(table, pageNumber);
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
