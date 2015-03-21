package org.ttdc.flipcards.server.sql;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.ttdc.flipcards.client.StudyWordsService;
import org.ttdc.flipcards.server.StudyItem;
import org.ttdc.flipcards.server.UploadService;
import org.ttdc.flipcards.shared.AutoCompleteWordPairList;
import org.ttdc.flipcards.shared.ItemFilter;
import org.ttdc.flipcards.shared.NotLoggedInException;
import org.ttdc.flipcards.shared.PagedWordPair;
import org.ttdc.flipcards.shared.QuizOptions;
import org.ttdc.flipcards.shared.Tag;
import org.ttdc.flipcards.shared.WordPair;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class StudyWordsServiceImpl extends RemoteServiceServlet implements
		StudyWordsService {
	private static final Logger LOG = Logger
			.getLogger(StudyWordsServiceImpl.class.getName());

	private String getUrl() {
		String url = null;
		try {
			if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
				// Load the class that provides the new "jdbc:google:mysql://"
				// prefix.
				Class.forName("com.mysql.jdbc.GoogleDriver");
				url = "jdbc:google:mysql://flipcards-ttdc:ttdc-gsql/FLIPCARDS?user=root";
			} else {
				// Local MySQL instance to use during development.
				Class.forName("com.mysql.jdbc.Driver");
				url = "jdbc:mysql://127.0.0.1:3306/test?user=root&password=password";

				// Alternatively, connect to a Google Cloud SQL instance using:
				// jdbc:mysql://ip-address-of-google-cloud-sql-instance:3306/guestbook?user=root
			}
		} catch (Exception e) {
			LOG.severe(e.getLocalizedMessage());
		}
		return url;
	}

	private Connection getConnection() throws SQLException {
		Connection conn = DriverManager.getConnection(getUrl());
		return conn;
	}
	private void closeConnection(Connection conn) {
		try {
			conn.close();
		} catch (SQLException e) {
			logException(e);
		}
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
	public WordPair addWordPair(String word, String definition)
			throws IllegalArgumentException, NotLoggedInException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WordPair updateWordPair(String id, String word, String definition)
			throws IllegalArgumentException, NotLoggedInException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean deleteWordPair(String id) throws IllegalArgumentException,
			NotLoggedInException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WordPair> generateQuiz(QuizOptions quizOptions)
			throws NotLoggedInException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFileUploadUrl() throws NotLoggedInException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void assignSelfToUserlessWords() throws NotLoggedInException {
		throw new RuntimeException("Deprecated");

	}

	@Override
	public List<Tag> getAllTagNames() throws IllegalArgumentException,
			NotLoggedInException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Tag createTag(String name) throws IllegalArgumentException,
			NotLoggedInException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteTagName(String tagId) throws IllegalArgumentException,
			NotLoggedInException {
		// TODO Auto-generated method stub

	}

	@Override
	public Tag updateTagName(String tagId, String name)
			throws IllegalArgumentException, NotLoggedInException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deTag(String tagId, String cardId)
			throws IllegalArgumentException, NotLoggedInException {
		// TODO Auto-generated method stub

	}

	@Override
	public void applyTag(String tagId, String cardId)
			throws IllegalArgumentException, NotLoggedInException {
		// TODO Auto-generated method stub

	}

	/*
	 * 
	 * INSERT INTO `FLIPCARDS`.`user` (`userId`, `email`) VALUES
	 * ('32e7b927-cfde-11e4-bbaf-14dae9f4dacd', 'trevisthomas@gmail.com');
	 * INSERT INTO `FLIPCARDS`.`user` (`userId`, `email`) VALUES
	 * ('3e284731-cfde-11e4-bbaf-14dae9f4dacd', 'chrissyannthomas@gmail.com');
	 */
	@Override
	public List<String> getStudyFriends() throws IllegalArgumentException,
			NotLoggedInException {
		List<String> users = new ArrayList<String>();
		try {
			Connection conn = getConnection();
			// At some point this method should probably only find the logged in
			// users chosen friends.
			String statement = "SELECT * FROM user";
			PreparedStatement stmt = conn.prepareStatement(statement);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				users.add(rs.getString("email"));
			}
		} catch (SQLException e) {
			logException(e);
		}
		return users;
	}

	@Override
	public WordPair setActiveStatus(String id, boolean active)
			throws IllegalArgumentException, NotLoggedInException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WordPair> getWordPairsForPage(int pageNumber)
			throws IllegalArgumentException, NotLoggedInException {
		throw new RuntimeException("Deprecated");
	}

	@Override
	public PagedWordPair getWordPairs(List<String> tagIds, List<String> users,
			ItemFilter filter, int pageNumber, int perPage)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();

		if (users.isEmpty()) {
			users = getStudyFriends();
		}

//		List<WordPair> wordPairs = new ArrayList<>();
		PagedWordPair pwp = null;

		if (tagIds.isEmpty()) {
			switch (filter) {
			case ACTIVE:
//				pwp = getWordPairsActive(users, pageNumber, perPage);
				break;
			case INACTIVE:
//				pwp = getWordPairInactive(users, pageNumber, perPage);
				break;
			case BOTH:
			default:
				pwp = getWordPairAll(users, pageNumber, perPage);
				break;
			}

		} else {
//			pwp = getWordPairsWithTags(tagIds, users, filter, pageNumber, perPage);
		}
		return pwp;
	}
	
	private PagedWordPair getWordPairAll(List<String> owners, int pageNumber,
			int pageSize) throws NotLoggedInException {
		PagedWordPair pagedWordPair = new PagedWordPair();
		List<WordPair> wordPairs = new ArrayList<>();
		Connection conn = null;
		try {
			conn = getConnection();
			if (pageNumber == 1) {
				String statement = "SELECT count(*) as total FROM study_item";
				PreparedStatement stmt = conn.prepareStatement(statement);
				ResultSet rs = stmt.executeQuery();
				rs.next();
				pagedWordPair.setTotalCardCount(rs.getLong("total"));
			}
			
			
			int start = (pageNumber - 1) * pageSize;
//			int end = start + pageSize;
			
			String statement = 		
			"SELECT si.studyItemId as 'studyItemId', si.word as 'word', si.definition as 'definition', u.email as 'email', " +
			"sm.viewCount as 'viewCount', sm.incorrectCount as 'incorrectCount', sm.difficulty as 'difficulty', " + 
			"sm.averageTime as 'averageTime', CASE WHEN sm.difficulty IS NOT null THEN true WHEN sm.difficulty IS null THEN false END as 'active', " + 
			"sm.lastUpdate as 'lastUpdate' " +
			"FROM study_item si " +
				"INNER JOIN user u " +
					"ON si.ownerId = u.userId " +
			    "LEFT OUTER JOIN study_item_meta sm " +
					"ON si.studyItemId = sm.studyItemId " +
			"ORDER BY si.createDate DESC " +
			"LIMIT "+start+","+pageSize ;
			
			System.err.println(statement);
			
//			q.setRange(start, end);

			PreparedStatement stmt = conn.prepareStatement(statement);
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				WordPair pair = new WordPair(rs.getString("studyItemId"), rs.getString("word"), rs.getString("definition"));
				pair.setActive(rs.getBoolean("active"));
				if(pair.isActive()){
					pair.setCorrectCount(rs.getLong("viewCount")
							- rs.getLong("incorrectCount"));
					pair.setTestedCount(rs.getLong("viewCount"));
					pair.setDifficulty(rs.getDouble("difficulty"));
					pair.setAverageTime(rs.getLong("averageTime"));
					if(rs.getTimestamp("lastUpdate") != null){
						pair.setLastUpdate(new Date(rs.getTimestamp("lastUpdate").getTime()));
					} else {
						pair.setLastUpdate(new Date());
					}
				}
				pair.setUser(rs.getString("email"));
				if (getUser().getEmail().equals(pair.getUser())) {
					pair.setDeleteAllowed(true);
				}
				wordPairs.add(pair);
			}
			
			
			

//			q.setOrdering("createDate desc");
//			int start = (pageNumber - 1) * pageSize;
//			int end = start + pageSize;
//			q.setRange(start, end);
//			List<StudyItem> cards = (List<StudyItem>) q.execute(owners);
//
//			for (StudyItem card : cards) {
//				WordPair pair = convert(card,
//						findMetaData(card.getId(), card.getOwner()));
//				wordPairs.add(pair);
//			}
//
			pagedWordPair.setWordPair(wordPairs);

		} catch (Exception e) {
			logException(e);
		} finally {
			closeConnection(conn);
		}
		return pagedWordPair;
	}

	

	@Override
	public AutoCompleteWordPairList getAutoCompleteWordPairs(
			List<String> owners, int sequence, String qstr)
			throws IllegalArgumentException, NotLoggedInException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WordPair getStudyItem(String studyItemId)
			throws IllegalArgumentException, NotLoggedInException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void answerQuestion(String id, long duration, Boolean correct)
			throws IllegalArgumentException, NotLoggedInException {
		// TODO Auto-generated method stub

	}
	
	private void logException(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		LOG.severe(sw.toString());
	}

	@Override
	public boolean migrate(int table, int pageNumber)
			throws NotLoggedInException {
		throw new RuntimeException("Deprecated");
	}

}
