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
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.ttdc.flipcards.client.StudyWordsService;
import org.ttdc.flipcards.server.StudyItem;
import org.ttdc.flipcards.server.StudyItemMeta;
import org.ttdc.flipcards.server.UploadService;
import org.ttdc.flipcards.shared.AutoCompleteWordPairList;
import org.ttdc.flipcards.shared.CardOrder;
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
import com.mysql.fabric.xmlrpc.base.Array;

public class StudyWordsServiceImpl extends RemoteServiceServlet implements
		StudyWordsService {
	private static final Logger LOG = Logger
			.getLogger(StudyWordsServiceImpl.class.getName());
	
	private final static String SELECT_EVERYTHING = "SELECT si.studyItemId as 'studyItemId', si.word as 'word', si.definition as 'definition', u.email as 'email', " +
			"sm.viewCount as 'viewCount', sm.incorrectCount as 'incorrectCount', sm.difficulty as 'difficulty', " + 
			"sm.averageTime as 'averageTime', sm.lastUpdate as 'lastUpdate', " +
			"sm.confidence as 'confidence', sm.totalTime as 'totalTime', sm.timedViewCount as 'timedViewCount' ";

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
	public WordPair addWordPair(String word, String definition, List<String> tagIds)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		Connection conn = null;
		try {
			
			validate(word, definition);
			
			conn = getConnection();
			exists("", word, conn); 
			UUID uuid = java.util.UUID.randomUUID();
			
			String statement = "INSERT INTO study_item (studyItemId, ownerId, createDate, word, definition) VALUES( ?, ?, ?, ?, ?)";
			PreparedStatement stmt = conn.prepareStatement(statement);
			stmt.setString(1, uuid.toString());
			stmt.setString(2, getUserIdForEmail(getUser().getEmail()));
			stmt.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
			stmt.setString(4, word);
			stmt.setString(5, definition);
			int success = 2;
			success = stmt.executeUpdate();
			if (success == 0) {
				throw new RuntimeException("Failed to insert StudyItem");
			}
			
			for(String tagId : tagIds){
				applyTag(tagId, uuid.toString());
			}
			
			return performGetWordPair(uuid.toString(), conn);
			
		} catch (SQLException e) {
			logException(e);
			throw new RuntimeException("Database error. Failed to insert StudyItem");
		} finally {
			closeConnection(conn);
		}
	}

	private void validate(String word, String definition) {
		if(word.trim().isEmpty()){
			throw new IllegalArgumentException("Word is invalid");
		}
		
		if(definition.trim().isEmpty()){
			throw new IllegalArgumentException("Definition is invalid");
		}
	}
	
	private void exists(String studyItemId, String word, Connection conn) throws SQLException{
		StringBuilder statement = new StringBuilder("SELECT si.studyItemId, si.word FROM study_item si WHERE si.word='");
		statement.append(word).append("'");
		PreparedStatement stmt = conn.prepareStatement(statement.toString());
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			String id = rs.getString(1);
			String str = rs.getString(2);
			if(str.equals(word)){
				if(!id.equals(studyItemId)){
					throw new IllegalArgumentException("Already exists");
				}
			}
		}
	}

	@Override
	public WordPair updateWordPair(String id, String word, String definition)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		Connection conn = null;
		
		try {
			validate(word, definition);
			conn = getConnection();
			exists(id, word, conn);
			
			String statement = "UPDATE study_item SET word = ?, definition=? WHERE studyItemId = ?";
			PreparedStatement stmt = conn.prepareStatement(statement);
			stmt.setString(1, word);
			stmt.setString(2, definition);
			stmt.setString(3, id);
			int success = 2;
			success = stmt.executeUpdate();
			if (success == 0) {
				throw new IllegalArgumentException("Failed to insert StudyItem");
			}
			
			return performGetWordPair(id, conn);
			
		} catch (SQLException e) {
			logException(e);
			throw new IllegalArgumentException("Internal error data not updated.");
		} finally {
			closeConnection(conn);
		}
	}

	private WordPair performGetWordPair(String id, Connection conn) throws SQLException {
		List<WordPair> wordPairs = new ArrayList<>();
		StringBuffer clause = new StringBuffer();
		clause.append(SELECT_EVERYTHING);
		clause.append("FROM study_item si INNER JOIN user u ON si.ownerId = u.userId  ");
		clause.append("LEFT OUTER JOIN study_item_meta sm ON si.studyItemId = sm.studyItemId AND sm.ownerId='"+getUserIdForEmail(getUser().getEmail())+"' ");
		clause.append("WHERE si.studyItemId='").append(id).append("'");
		
		executeWordPairQueryAndTagResults(wordPairs, conn, clause.toString());
		return wordPairs.get(0);
	}

	@Override
	public Boolean deleteWordPair(String id) throws IllegalArgumentException,
			NotLoggedInException {
		checkLoggedIn();
		Connection conn = null;
		
		try {
			conn = getConnection();
			
			String statement = "SELECT u.email FROM study_item_meta sm INNER JOIN user u ON sm.ownerId = u.userId  WHERE studyItemId = ?";
			PreparedStatement stmt = conn.prepareStatement(statement);
			stmt.setString(1, id);
			ResultSet rs = stmt.executeQuery();
			String pwnedError = "";
			while(rs.next()){
				if(!pwnedError.isEmpty()){
					pwnedError += ", ";
				}
				pwnedError += rs.getString(1);
			}
			
			if(!pwnedError.isEmpty()){
				throw new IllegalArgumentException("Can't delete.  Word is still active by " + pwnedError +".");
			}
			
			
			conn.setAutoCommit(false);
			
			statement = "DELETE FROM study_item WHERE studyItemId = ?";
			PreparedStatement stmtStudyItem = conn.prepareStatement(statement);
			stmtStudyItem.setString(1, id);
			stmtStudyItem.executeUpdate();
			
			statement = "DELETE FROM tag_association WHERE studyItemId = ?";
			PreparedStatement stmtTagAss = conn.prepareStatement(statement);
			stmtTagAss.setString(1, id);
			stmtTagAss.executeUpdate();
			
			conn.commit();
			return true;
			
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				LOG.severe("Rollback threw an error!");
			}
			logException(e);
			throw new IllegalArgumentException("Internal error data not updated.");
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public List<WordPair> generateQuiz(QuizOptions quizOptions)
			throws NotLoggedInException {
		
		checkLoggedIn();
		PagedWordPair pwp = performGetWordPairs(quizOptions.getTagIds(), null, ItemFilter.ACTIVE, quizOptions.getCardOrder(), 1, quizOptions.getSize());
		return pwp.getWordPair();
	}

	@Override
	public String getFileUploadUrl() throws NotLoggedInException {
		throw new RuntimeException("Deprecated");
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
		checkLoggedIn();
		Connection conn = null;
		try {
			if(name == null || name.trim().isEmpty()){
				throw new IllegalArgumentException("Tag's can't be blank!");
			}
			conn = getConnection();
			UUID uuid = java.util.UUID.randomUUID();
			
			String statement = "INSERT INTO tag (tagId, tagName, ownerId) VALUES(?, ?, ?)";
			PreparedStatement stmt = conn.prepareStatement(statement);
			stmt.setString(1, uuid.toString());
			stmt.setString(2, name);
			stmt.setString(3, getUserIdForEmail(getUser().getEmail()));
			int success = 2;
			success = stmt.executeUpdate();
			if (success == 0) {
				throw new RuntimeException("Failed to create tag.");
			}
			return new Tag(uuid.toString(), name);
			
		} catch (SQLException e) {
			logException(e);
			throw new RuntimeException("Database error. Failed to create tag.");
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public void deleteTagName(String tagId) throws IllegalArgumentException,
			NotLoggedInException {
		checkLoggedIn();
		Connection conn = null;
		
		try {
			conn = getConnection();
			
			conn.setAutoCommit(false);
			
			String statement = "DELETE FROM tag WHERE tagId = ?";
			PreparedStatement stmtStudyItem = conn.prepareStatement(statement);
			stmtStudyItem.setString(1, tagId);
			stmtStudyItem.executeUpdate();
			
			statement = "DELETE FROM tag_association WHERE tagId = ?";
			PreparedStatement stmtTagAss = conn.prepareStatement(statement);
			stmtTagAss.setString(1, tagId);
			stmtTagAss.executeUpdate();
			
			conn.commit();
			
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				LOG.severe("Rollback threw an error!");
			}
			logException(e);
			throw new IllegalArgumentException("Internal error data not updated.");
		} finally {
			closeConnection(conn);
		}

	}

	@Override
	public Tag updateTagName(String tagId, String name)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		Connection conn = null;
		
		if(name == null || name.trim().isEmpty()){
			throw new IllegalArgumentException("Tag's can't be blank!");
		}
		
		try {
			conn = getConnection();
			
			String statement = "UPDATE tag SET tagName = ? WHERE tagId = ?";
			PreparedStatement stmt = conn.prepareStatement(statement);
			stmt.setString(1, name);
			stmt.setString(2, tagId);
			
			int success = 2;
			success = stmt.executeUpdate();
			if (success == 0) {
				throw new IllegalArgumentException("Failed to update tag");
			}
			
			return new Tag(tagId, name);
		} catch (SQLException e) {
			logException(e);
			throw new IllegalArgumentException("Internal error data not updated.");
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public void deTag(String tagId, String studyItemId)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		Connection conn = null;
		try {
			conn = getConnection();
			
			String statement = "DELETE FROM tag_association WHERE studyItemId = ? && tagId= ?";
			PreparedStatement stmt = conn.prepareStatement(statement);
			stmt.setString(1, studyItemId);
			stmt.setString(2, tagId);
			int success = 2;
			success = stmt.executeUpdate();
			if (success != 1) {
				throw new RuntimeException("Failed to remove tag");
			}
			
		} catch (SQLException e) {
			logException(e);
			throw new RuntimeException("Database error. Failed to remove tag");
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public void applyTag(String tagId, String studyItemId)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		Connection conn = null;
		try {
			conn = getConnection();
			UUID uuid = java.util.UUID.randomUUID();
			
			String statement = "INSERT INTO tag_association (tagAssId, studyItemId, ownerId, tagId) VALUES( ?, ?, ?, ?)";
			PreparedStatement stmt = conn.prepareStatement(statement);
			stmt.setString(1, uuid.toString());
			stmt.setString(2, studyItemId);
			stmt.setString(3, getUserIdForEmail(getUser().getEmail()));
			stmt.setString(4, tagId);
			int success = 2;
			success = stmt.executeUpdate();
			if (success == 0) {
				throw new RuntimeException("Failed to apply tag");
			}
			
		} catch (SQLException e) {
			logException(e);
			throw new RuntimeException("Database error. Failed to apply tag");
		} finally {
			closeConnection(conn);
		}

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
				users.add(rs.getString("userId"));
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
		checkLoggedIn();
		Connection conn = null;
		
		try {
			conn = getConnection();
			
			if (active) {
				
				WordPair wp = performGetWordPair(id, conn);
				
				//Somehow this happened in prod so i coded the backend not to allow it.
				if(wp.isActive()){
					throw new IllegalArgumentException("Already active!");
				}
				
				String statement = "INSERT INTO study_item_meta (studyItemMetaId, ownerId, studyItemId, createDate) VALUES( ?, ?, ?, ?)";
				PreparedStatement stmt = conn.prepareStatement(statement);
				
				UUID uuid = java.util.UUID.randomUUID();
				stmt.setString(1, uuid.toString());
				stmt.setString(2, getUserIdForEmail(getUser().getEmail()));
				stmt.setString(3, id);
				stmt.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
				int success = 2;
				success = stmt.executeUpdate();
				if (success == 0) {
					throw new IllegalArgumentException("Failed to activate"); 
				}
				
			} else {
				String statement = "DELETE FROM study_item_meta WHERE studyItemId = ? && ownerId= ?";
				PreparedStatement stmtStudyItemMeta = conn.prepareStatement(statement);
				stmtStudyItemMeta.setString(1, id);
				stmtStudyItemMeta.setString(2, getUserIdForEmail(getUser().getEmail()));
				int success = 2;
				success = stmtStudyItemMeta.executeUpdate();
				if (success != 1) {
					throw new IllegalArgumentException("Failed to deactivate"); 
				}
			}
			
			return performGetWordPair(id, conn);
			
		} catch (SQLException e) {
			logException(e);
			throw new IllegalArgumentException("Internal error data not updated.");
		} finally {
			closeConnection(conn);
		}
	}
	

	@Override
	public List<WordPair> getWordPairsForPage(int pageNumber)
			throws IllegalArgumentException, NotLoggedInException {
		throw new RuntimeException("Deprecated");
	}

	@Override
	public PagedWordPair getWordPairs(List<String> tagIds, List<String> users,
			ItemFilter filter, CardOrder cardOrder, int pageNumber, int perPage)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		List<String> userIds = new ArrayList<String>();
//		if (users.isEmpty()) {
//			userIds = getStudyFriends();
//		} else {
//			for(String email : users){
//				userIds.add(getUserIdForEmail(email));
//			}
//		}
		
		return performGetWordPairs(tagIds, users, filter, cardOrder, pageNumber, perPage);
	}
	
	

	/**
	 * One query to rule them all!
	 * 
	 * @param tagIds
	 * @param owners
	 * @param filter
	 * @param order
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	private PagedWordPair performGetWordPairs(List<String> tagIds,
			List<String> users, ItemFilter filter, CardOrder order, int pageNumber, int pageSize) {

		PagedWordPair pagedWordPair = new PagedWordPair();
		List<WordPair> wordPairs = new ArrayList<>();
		Connection conn = null;
		StringBuilder fromClause = new StringBuilder();
		StringBuilder whereClause = new StringBuilder();
		try {
			conn = getConnection();
			fromClause.append("FROM study_item si INNER JOIN user u ON si.ownerId = u.userId ");
			
			switch (filter) {
			case ACTIVE:
				fromClause.append("INNER JOIN study_item_meta sm ON si.studyItemId = sm.studyItemId AND sm.ownerId='"+getUserIdForEmail(getUser().getEmail())+"' ");
				break;
			case INACTIVE:
			case BOTH:
			default:
				fromClause.append("LEFT OUTER JOIN study_item_meta sm ON si.studyItemId = sm.studyItemId AND sm.ownerId='"+getUserIdForEmail(getUser().getEmail())+"' ");
				break;
			}
			
			if(ItemFilter.INACTIVE.equals(filter)){
				appendWhereOrAnd(whereClause);
				whereClause.append("si.studyItemId NOT IN (SELECT sm.studyItemId FROM study_item_meta sm WHERE sm.ownerId='"+getUserIdForEmail(getUser().getEmail())+"') ");
			}
			if(!tagIds.isEmpty()){
				appendWhereOrAnd(whereClause);
				whereClause.append("si.studyItemId IN (SELECT ta.studyItemId FROM tag_association ta WHERE ta.tagId IN (");
						createCommaSeperatedListOfStrings(tagIds, whereClause);	
						whereClause.append(") ) ");						
			}
			
			
			List<String> userIds = new ArrayList<String>();
			if(users == null || users.isEmpty()){
				userIds = getStudyFriends();
			}
			else {
				for(String email : users){
					userIds.add(getUserIdForEmail(email));
				}
			}
			
			if(userIds != null){		
				appendWhereOrAnd(whereClause);
				whereClause.append(" si.ownerId IN (");
				createCommaSeperatedListOfStrings(userIds, whereClause);
				whereClause.append(") ");
			}
			
			if (pageNumber == 1) {
				StringBuilder statement = new StringBuilder();

				statement.append("SELECT count(*) as total ") 
				.append(fromClause)
				.append(whereClause);
				
				LOG.info(statement.toString());
				
				PreparedStatement stmt = conn.prepareStatement(statement.toString());
				ResultSet rs = stmt.executeQuery();
				rs.next();
				pagedWordPair.setTotalCardCount(rs.getLong("total"));
			}
			
			int start = (pageNumber - 1) * pageSize;

			StringBuilder statement = new StringBuilder();
			statement.append(SELECT_EVERYTHING);
			statement.append(fromClause)
			.append(whereClause);
			switch(order){
				case HARDEST:
					statement.append("ORDER BY sm.difficulty DESC, sm.createDate DESC  ");
					break;
				case EASIEST:
					statement.append("ORDER BY sm.difficulty ASC, sm.createDate DESC  ");
					break;
				case LATEST_ADDED:
					statement.append("ORDER BY si.createDate DESC ");
					break;
				case LEAST_RECIENTLY_STUDIED:
					statement.append("ORDER BY sm.lastUpdate ASC ");
					break;
				case LEAST_STUDIED:
					statement.append("ORDER BY sm.viewCount ASC, sm.createDate DESC  ");
					break;
				case RANDOM:
					statement.append("ORDER BY RAND() ");
					break;
				case SLOWEST:
					statement.append("ORDER BY sm.averageTime DESC, sm.createDate DESC  ");
					break;
				case TERM:
					statement.append("ORDER BY si.word ");
					break;
					
				case TERM_DES:
					statement.append("ORDER BY si.word DESC ");
					break;	
				default:
					statement.append("ORDER BY si.createDate DESC, sm.createDate DESC  ");
			}
			
			statement.append("LIMIT ").append(start).append(",").append(pageSize);
			
			executeWordPairQueryAndTagResults(wordPairs, conn, statement.toString());
			
			pagedWordPair.setWordPair(wordPairs);

		} catch (Exception e) {
			logException(e);
		} finally {
			closeConnection(conn);
		}
		return pagedWordPair;
		
	}

	private void appendWhereOrAnd(StringBuilder stringBuilder) {
		if(stringBuilder.toString().isEmpty()){
			stringBuilder.append(" WHERE ");
		} else {
			stringBuilder.append(" AND ");
		}
	}

	private void executeWordPairQueryAndTagResults(List<WordPair> wordPairs,
			Connection conn, String statement) throws SQLException {
		Map<String, WordPair> map = new HashMap<String, WordPair>();

		LOG.info(statement.toString());
		PreparedStatement stmt = conn.prepareStatement(statement);
		ResultSet rs = stmt.executeQuery();
		while(rs.next()){
			WordPair pair = new WordPair(rs.getString("studyItemId"), rs.getString("word"), rs.getString("definition"));
			pair.setActive(rs.getObject("difficulty") != null);
			if(pair.isActive()){
				pair.setIncorrectCount(rs.getLong("incorrectCount"));
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
				pair.setConfidence(rs.getDouble("confidence"));
				pair.setTotalTime(rs.getLong("totalTime"));
				pair.setTimedViewCount(rs.getLong("timedViewCount"));
				
				
			}
			pair.setUser(rs.getString("email"));
			if (getUser().getEmail().equals(pair.getUser())) {
				pair.setDeleteAllowed(true);
			}
			wordPairs.add(pair);
			map.put(pair.getId(), pair);
		}
		
		if(map.isEmpty()){
			return;
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
		
		Connection conn = null;
		AutoCompleteWordPairList list = null;
		try{
			conn = getConnection();
			List<WordPair> wordPairs = new ArrayList<WordPair>();
			StringBuilder statement = new StringBuilder("SELECT si.studyItemId, si.word, si.definition FROM study_item si WHERE si.word LIKE ? LIMIT 10");
			PreparedStatement stmt = conn.prepareStatement(statement.toString());
			stmt.setString(1, qstr+"%");
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				wordPairs.add(new WordPair(rs.getString(1), rs.getString(2), rs.getString(3)));
			}
			list = new AutoCompleteWordPairList(sequence, wordPairs);
		} catch (Exception e) {
			logException(e);
		} finally {
			closeConnection(conn);
		}
		return list;
	}

	@Override
	public WordPair getStudyItem(String studyItemId)
			throws IllegalArgumentException, NotLoggedInException {
		
		checkLoggedIn();
		Connection conn = null;
		try {
			conn = getConnection();
			return performGetWordPair(studyItemId, conn);
		} catch (Exception e) {
			logException(e);
			throw new IllegalArgumentException("Failed to load data from database.");
		} finally {
			closeConnection(conn);
		}
	}

	@Override
	public void answerQuestion(String id, long duration, Boolean correct)
			throws IllegalArgumentException, NotLoggedInException {
		checkLoggedIn();
		Connection conn = null;
		try {
			conn = getConnection();
			
			WordPair wp = getStudyItem(id);
			if (wp == null) {
				throw new IllegalArgumentException("Failed to update score.");
			}
			
			
			if (!correct) {
				wp.setIncorrectCount(wp.getIncorrectCount() + 1);
			}
			wp.setLastUpdate(new Date());
			wp.setTestedCount(wp.getTestedCount() + 1);
			wp.setTimedViewCount(wp.getTimedViewCount() + 1); //This was added so that cards that existed before timing mattered will have accurate averages.  It would also allow you to have non-timed runs, but i'm not implementing that now.
			
			wp.setDifficulty((double) wp
					.getIncorrectCount()
					/ (double) wp.getTestedCount());
			
			wp.setTotalTime(wp.getTotalTime() + duration);
			wp.setAverageTime(wp.getTotalTime() / wp.getTimedViewCount());
			
			//Now apply the update

			String statement = "UPDATE study_item_meta sm SET incorrectCount = ?, viewCount = ?, lastUpdate=?,"
					+ "difficulty = ?, confidence = ?, totalTime = ?, averageTime = ?, timedViewCount = ? "
					+ "WHERE sm.studyItemId = ? AND sm.ownerId = ?";
			
			PreparedStatement stmt = conn.prepareStatement(statement);
			conn.setAutoCommit(false);
			
			
			stmt.setLong(1, wp.getIncorrectCount());
			stmt.setLong(2, wp.getTestedCount());
			if(wp.getLastUpdate() != null){
				stmt.setTimestamp(3, new java.sql.Timestamp(wp.getLastUpdate().getTime()));
			}
			else {
				stmt.setTimestamp(3, null);
			}
			stmt.setDouble(4, wp.getDifficulty());
			stmt.setDouble(5, wp.getConfidence());
			stmt.setLong(6, wp.getTotalTime());
			stmt.setLong(7, wp.getAverageTime());
			stmt.setLong(8, wp.getTimedViewCount());
			stmt.setString(9, id);
			stmt.setString(10, getUserIdForEmail(getUser().getEmail()));

			int success = 2;
			success = stmt.executeUpdate();
			if (success == 1) {
				conn.commit();
			}
			else {
				conn.rollback();
				throw new IllegalArgumentException("Update failed to update one record!");
			}
		} catch (Exception e) {
			logException(e);
			throw new IllegalArgumentException("Failed to update score!");
		} finally {
			closeConnection(conn);
		}
	
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

