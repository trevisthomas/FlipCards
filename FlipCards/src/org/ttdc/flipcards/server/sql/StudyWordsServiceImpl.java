package org.ttdc.flipcards.server.sql;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.ttdc.flipcards.client.StudyWordsService;
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
		if(conn == null){ 
			return;
		}
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
		List<Tag> tags = new ArrayList<Tag>();
		Connection conn = null;
		try {
			conn = getConnection();
			String statement = "SELECT * FROM tag";
			PreparedStatement stmt = conn.prepareStatement(statement);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				tags.add(new Tag(rs.getString("tagId"), rs.getString("tagName")));
			}
		} catch (SQLException e) {
			logException(e);
		} finally {
			closeConnection(conn);
		}
		return tags;
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
		Connection conn = null;
		try {
			conn = getConnection();
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
		} finally {
			closeConnection(conn);
		}
		return users;
	}
	
	private String getUserIdForEmail(String email) throws SQLException{
		Connection conn = getConnection();
		// At some point this method should probably only find the logged in
		// users chosen friends.
		String statement = "SELECT * FROM user WHERE user.email='"+email+"'";
		PreparedStatement stmt = conn.prepareStatement(statement);
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			return rs.getString("userId");
		}
		throw new RuntimeException("Email not found: "+email );
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
				pwp = getWordPairsActive(users, pageNumber, perPage);
				break;
			case INACTIVE:
				pwp = getWordPairInactive(users, pageNumber, perPage);
				break;
			case BOTH:
			default:
				pwp = getWordPairAll(users, pageNumber, perPage);
				break;
			}

		} else {
			pwp = getWordPairsWithTags(tagIds, users, filter, pageNumber, perPage);
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

			String statement = 		
			"SELECT si.studyItemId as 'studyItemId', si.word as 'word', si.definition as 'definition', u.email as 'email', " +
			"sm.viewCount as 'viewCount', sm.incorrectCount as 'incorrectCount', sm.difficulty as 'difficulty', " + 
			"sm.averageTime as 'averageTime', sm.lastUpdate as 'lastUpdate' " +
			"FROM study_item si " +
				"INNER JOIN user u " +
					"ON si.ownerId = u.userId " +
			    "LEFT OUTER JOIN study_item_meta sm " +
					"ON si.studyItemId = sm.studyItemId AND sm.ownerId='"+getUserIdForEmail(getUser().getEmail())+"'"+
            "ORDER BY si.createDate DESC " +
			"LIMIT "+start+","+pageSize ;
			
			LOG.info(statement);
			
			executeWordPairQueryAndTagResults(wordPairs, conn, statement);
			
			pagedWordPair.setWordPair(wordPairs);

		} catch (Exception e) {
			logException(e);
		} finally {
			closeConnection(conn);
		}
		return pagedWordPair;
	}
	
	private PagedWordPair getWordPairInactive(List<String> owners, int pageNumber,
			int pageSize) throws NotLoggedInException {
		PagedWordPair pagedWordPair = new PagedWordPair();
		List<WordPair> wordPairs = new ArrayList<>();
		Connection conn = null;
		StringBuilder fromClause = new StringBuilder();
		
		try {
			conn = getConnection();
			
			fromClause.append("FROM study_item si INNER JOIN user u ON si.ownerId = u.userId  "
					+ "WHERE si.studyItemId NOT IN "
					+ "(SELECT sm.studyItemId FROM study_item_meta sm WHERE sm.ownerId='"+getUserIdForEmail(getUser().getEmail())+"') ");
					
			if(owners != null){		
				fromClause.append(" AND u.email IN (");
				createCommaSeperatedListOfStrings(owners, fromClause);
				fromClause.append(") ");
			}
			
			if (pageNumber == 1) {
				StringBuilder statement = new StringBuilder();

				statement.append("SELECT count(*) as total "); 
				
				statement.append(fromClause);
				
				PreparedStatement stmt = conn.prepareStatement(statement.toString());
				ResultSet rs = stmt.executeQuery();
				rs.next();
				pagedWordPair.setTotalCardCount(rs.getLong("total"));
			}
			
			int start = (pageNumber - 1) * pageSize;

			StringBuilder statement = new StringBuilder();
			statement.append("SELECT si.studyItemId as 'studyItemId', si.word as 'word', si.definition as 'definition', u.email as 'email', null as 'viewCount', null as 'incorrectCount', null as 'difficulty', null as 'averageTime', null as 'lastUpdate' ");
			statement.append(fromClause)
			.append("ORDER BY si.createDate DESC ")
			.append("LIMIT ").append(start).append(",").append(pageSize);
			
			LOG.info(statement.toString());
			
			executeWordPairQueryAndTagResults(wordPairs, conn, statement.toString());
			
			pagedWordPair.setWordPair(wordPairs);

		} catch (Exception e) {
			logException(e);
		} finally {
			closeConnection(conn);
		}
		return pagedWordPair;
	}
	
	private PagedWordPair getWordPairsActive(List<String> users, int pageNumber, int pageSize) {
		PagedWordPair pagedWordPair = new PagedWordPair();
		List<WordPair> wordPairs = new ArrayList<>();
		Connection conn = null;
		try {
			conn = getConnection();
			if (pageNumber == 1) {
				String statement = "SELECT count(*) as total FROM study_item_meta sm WHERE sm.ownerId='"+getUserIdForEmail(getUser().getEmail())+"'";
				PreparedStatement stmt = conn.prepareStatement(statement);
				ResultSet rs = stmt.executeQuery();
				rs.next();
				pagedWordPair.setTotalCardCount(rs.getLong("total"));
			}
			
			
			int start = (pageNumber - 1) * pageSize;

			String statement = 		
			"SELECT si.studyItemId as 'studyItemId', si.word as 'word', si.definition as 'definition', u.email as 'email', " +
			"sm.viewCount as 'viewCount', sm.incorrectCount as 'incorrectCount', sm.difficulty as 'difficulty', " + 
			"sm.averageTime as 'averageTime', sm.lastUpdate as 'lastUpdate' " +
			"FROM study_item si " +
				"INNER JOIN user u " +
					"ON si.ownerId = u.userId " +
			    "INNER JOIN study_item_meta sm " + //Inner join to exclude words that dont have meta
					"ON si.studyItemId = sm.studyItemId AND sm.ownerId='"+getUserIdForEmail(getUser().getEmail())+"'"+
            "ORDER BY si.createDate DESC " +
			"LIMIT "+start+","+pageSize ;
			
			LOG.info(statement);
			
			executeWordPairQueryAndTagResults(wordPairs, conn, statement);
			
			pagedWordPair.setWordPair(wordPairs);

		} catch (Exception e) {
			logException(e);
		} finally {
			closeConnection(conn);
		}
		return pagedWordPair;
	}
	
	private PagedWordPair getWordPairsWithTags(List<String> tagIds,
			List<String> owners, ItemFilter filter, int pageNumber, int pageSize) {

		PagedWordPair pagedWordPair = new PagedWordPair();
		List<WordPair> wordPairs = new ArrayList<>();
		Connection conn = null;
		StringBuilder fromClause = new StringBuilder();
		try {
			conn = getConnection();
			
			fromClause.append("FROM study_item si INNER JOIN user u ON si.ownerId = u.userId  "
					+ "WHERE si.studyItemId IN "
					+ "(SELECT ta.studyItemId FROM tag_association ta WHERE ta.tagId IN (");
					createCommaSeperatedListOfStrings(tagIds, fromClause);	
					fromClause.append(") )");						
			if(owners != null){		
				fromClause.append(" AND u.email IN (");
				createCommaSeperatedListOfStrings(owners, fromClause);
				fromClause.append(") ");
			}
			
			if (pageNumber == 1) {
				StringBuilder statement = new StringBuilder();

				statement.append("SELECT count(*) as total "); 
				
				statement.append(fromClause);
				
				PreparedStatement stmt = conn.prepareStatement(statement.toString());
				ResultSet rs = stmt.executeQuery();
				rs.next();
				pagedWordPair.setTotalCardCount(rs.getLong("total"));
			}
			
			int start = (pageNumber - 1) * pageSize;

			StringBuilder statement = new StringBuilder();
			statement.append("SELECT si.studyItemId as 'studyItemId', si.word as 'word', si.definition as 'definition', u.email as 'email', null as 'viewCount', null as 'incorrectCount', null as 'difficulty', null as 'averageTime', null as 'lastUpdate' ");
			statement.append(fromClause)
			.append("ORDER BY si.createDate DESC ")
			.append("LIMIT ").append(start).append(",").append(pageSize);
			
			LOG.info(statement.toString());
			
			executeWordPairQueryAndTagResults(wordPairs, conn, statement.toString());
			
			pagedWordPair.setWordPair(wordPairs);

		} catch (Exception e) {
			logException(e);
		} finally {
			closeConnection(conn);
		}
		return pagedWordPair;
		
	}

	private void executeWordPairQueryAndTagResults(List<WordPair> wordPairs,
			Connection conn, String statement) throws SQLException {
		Map<String, WordPair> map = new HashMap<String, WordPair>();

		PreparedStatement stmt = conn.prepareStatement(statement);
		ResultSet rs = stmt.executeQuery();
		while(rs.next()){
			WordPair pair = new WordPair(rs.getString("studyItemId"), rs.getString("word"), rs.getString("definition"));
			pair.setActive(rs.getObject("difficulty") != null);
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
			map.put(pair.getId(), pair);
		}
		
		//Query for all tags in the above wordPairs

		StringBuilder builder = new StringBuilder();
		builder.append("SELECT ta.studyItemId as 'studyItemId', t.tagName as 'name', t.tagId as 'tagId' FROM tag_association ta INNER JOIN tag t ON t.tagId=ta.tagId WHERE ta.studyItemId IN(");
		createCommaSeperatedListOfStrings(map.keySet(), builder);
		builder.append(")");
		statement = builder.toString();
		LOG.info(statement);
		stmt = conn.prepareStatement(statement);
		
		rs = stmt.executeQuery();
		
		while(rs.next()){
			WordPair wp = map.get(rs.getString("studyItemId"));
			wp.getTags().add(new Tag(rs.getString("tagId"), rs.getString("name")));
		}
	}

	private void createCommaSeperatedListOfStrings(Collection<String> collection,
			StringBuilder builder) {
		boolean first = true;
		for(String key : collection){
			if(!first){
				builder.append(",");
			} else {
				first = !first;
			}
			builder.append("'").append(key).append("'");
		}
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
