package org.ttdc.flipcards.server;

import java.util.ArrayList;
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

import org.ttdc.flipcards.client.StudyWordsService;
import org.ttdc.flipcards.shared.NotLoggedInException;
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
			WordPair pair = new WordPair(uuid.toString(), word, definition);

			pm.makePersistent(pair);
			return pair;
		} catch (Exception e) {
			LOG.log(Level.WARNING, e.getMessage());
			return null;
		} finally {
			pm.close();
		}

	}

	@Override
	public List<WordPair> getAllWordPairs() throws NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		// List<String> symbols = new ArrayList<String>();
		// return new ArrayList<>(wordPairs.values());

		List<WordPair> wordPairs = new ArrayList<>();
		try {
			Query q = pm.newQuery(WordPair.class);
			// q.declareParameters("com.google.appengine.api.users.User u");
			// q.setOrdering("createDate");
			List<WordPair> result = (List<WordPair>) q.execute();
			for (WordPair pair : result) {
				wordPairs.add(pair);
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

	@Override
	public Boolean deleteWordPair(String id) throws IllegalArgumentException,
			NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		long deleteCount = 0;
		try {
			Query q = pm.newQuery(WordPair.class, "id == identity");
			q.declareParameters("java.lang.String identity");
			List<WordPair> wordPairs = (List<WordPair>) q.execute(id);
			for (WordPair pair : wordPairs) {
				if (id.equals(pair.getId())) {
					deleteCount++;
					pm.deletePersistent(pair);
				}
			}
			if (deleteCount != 1) {
				LOG.log(Level.WARNING, "word pair deleted " + deleteCount
						+ " pairs. How is that possible?");
			}
		} finally {
			pm.close();
		}

		return deleteCount == 1;
	}
}
