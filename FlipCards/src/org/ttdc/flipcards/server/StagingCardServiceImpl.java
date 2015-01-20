package org.ttdc.flipcards.server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.ttdc.flipcards.client.services.StagingCardService;
import org.ttdc.flipcards.shared.NotLoggedInException;
import org.ttdc.flipcards.shared.Tag;
import org.ttdc.flipcards.shared.WordPair;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class StagingCardServiceImpl  extends RemoteServiceServlet implements StagingCardService{
	
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
			throws IllegalArgumentException, NotLoggedInException{
		StudyWordsServiceImpl.checkLoggedIn();
		UUID uuid = java.util.UUID.randomUUID();
		
		checkDoesTermExist(term, getUser());

		PersistenceManager pm = StudyWordsServiceImpl.getPersistenceManager();

		try {
			CardStaging card = new CardStaging(uuid.toString(), term, definition, getUser());
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
	public List<WordPair> getStagedCards() throws IllegalArgumentException,
			NotLoggedInException {
		StudyWordsServiceImpl.checkLoggedIn();
		
		PersistenceManager pm = StudyWordsServiceImpl.getPersistenceManager();
		List<WordPair> wordPairs = new ArrayList<>();
		try {
			Query q = pm.newQuery(CardStaging.class, "user == u");
			q.declareParameters("com.google.appengine.api.users.User u");
			q.setOrdering("createDate");
			List<CardStaging> cards = (List<CardStaging>) q.execute(getUser());
			
//			//Until i figure out how to import as a user, the user field will be null for imported ones.
//			Query q = pm.newQuery(CardStaging.class);
//			q.setOrdering("createDate");
//			List<CardStaging> cards = (List<CardStaging>) q.execute();
			
			int i = 1;
			for (CardStaging card : cards) {
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
	public void delete(String id) throws IllegalArgumentException,
			NotLoggedInException {
		PersistenceManager pm = StudyWordsServiceImpl.getPersistenceManager();
		try{
			Query q = pm.newQuery(CardStaging.class, "id == i");
			q.declareParameters("java.lang.String i");
			LOG.info("Deleted: "+q.deletePersistentAll(id) + " card.");
		} catch (Exception e) {
			LOG.info(e.getMessage());
			throw new IllegalArgumentException("failed to delete card from staging");
		}
		finally {
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
			List<CardStaging> cards  = (List<CardStaging>) q.execute(id);
			CardStaging card = cards.get(0);
			UUID uuid = java.util.UUID.randomUUID();
			
			if(StudyWordsServiceImpl.exists(card.getWord(), getUser()) != null){
				throw new IllegalArgumentException("A card with this term already exists in the card stack. Promotion refused.");
			}
			
			Card pair = new Card(uuid.toString(), card.getWord(), card.getDefinition(), getUser());
			pm.makePersistent(pair);
			pm.deletePersistent(card); 
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
		checkDoesTermExist(word, getUser());
		try {
			Query q = pm.newQuery(CardStaging.class, "id == i");
			q.declareParameters("java.lang.String i");
			List<CardStaging> cards  = (List<CardStaging>) q.execute(id);
			CardStaging card = cards.get(0);
			if(card != null){
				card.setWord(word);
				card.setDefinition(definition);
			}
			pm.flush();
			return convert(card);

		} catch (Exception e) {
			LOG.info(e.getMessage());
			throw new IllegalArgumentException("Failed to retrieve");
		} finally {
			pm.close();
		}
	}

	public static void checkDoesTermExist(String word, User user) {
		if(StudyWordsServiceImpl.exists(word, user) != null){
			throw new IllegalArgumentException("A card with this term already exists in the card stack.");
		}
		
		if(exists(word, user) != null){
			throw new IllegalArgumentException("A card with this term already exists in staging");
		}
	}
	
	
	WordPair convert(CardStaging pair){
		WordPair gwtPair;
		gwtPair = new WordPair(pair.getId(), pair.getWord(), pair.getDefinition());
		if(pair.getUser() != null){
			gwtPair.setUser(pair.getUser().getNickname());
		}
		return gwtPair;
	}
	
	public static CardStaging exists(String term, User user){
		PersistenceManager pm = StudyWordsServiceImpl.getPersistenceManager();
		try {
			Query q = pm.newQuery(CardStaging.class, "user == u && word == w");
			q.declareParameters("com.google.appengine.api.users.User u, java.lang.String w");
			
			List<CardStaging> cards = (List<CardStaging>) q.execute(user, term);
			
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
	
	
	
	
	
}
