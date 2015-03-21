package org.ttdc.flipcards.server.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

import org.ttdc.flipcards.server.PersistantTagName;
import org.ttdc.flipcards.server.StudyItem;
import org.ttdc.flipcards.server.StudyItemMeta;
import org.ttdc.flipcards.server.TagAssociation;

import com.google.appengine.api.utils.SystemProperty;

public class SqlDaoHelper {
	private static final Logger LOG = Logger.getLogger(SqlMigrator.class
			.getName());

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

	Connection getConnection() throws SQLException {
		Connection conn = DriverManager.getConnection(getUrl());
		return conn;
	}

	private String getHackedUserId(String email) {
		if (email != null
				&& (email.equals("chrissyannthomas@gmail.com") || email
						.equals("test2@example.com"))) {
			return "3e284731-cfde-11e4-bbaf-14dae9f4dacd";
		}
		// if(email.equals("trevisthomas@gmail.com") ||
		// email.equals("test@example.com")){
		else {
			return "32e7b927-cfde-11e4-bbaf-14dae9f4dacd";
		}
	}

	public void insert(Connection conn, TagAssociation tagAss)
			throws SQLException {
		// Connection conn = DriverManager.getConnection(getUrl());
		try {

			String statement = "INSERT INTO tag_association (tagAssId, studyItemId, ownerId, tagId) VALUES( ?, ?, ?, ?)";
			PreparedStatement stmt = conn.prepareStatement(statement);
			UUID uuid = java.util.UUID.randomUUID();
			stmt.setString(1, uuid.toString());
			stmt.setString(2, tagAss.getCardId());
			stmt.setString(3, getHackedUserId(tagAss.getCardOwner()));
			stmt.setString(4, tagAss.getTagId());
			int success = 2;
			success = stmt.executeUpdate();
			if (success == 0) {
				LOG.severe("Failed to insert tag association");
			}

		} finally {
			// conn.close();
		}

	}

	public void insert(Connection conn, StudyItem item) throws SQLException {
		// Connection conn = DriverManager.getConnection(getUrl());
		try {

			String statement = "INSERT INTO study_item (studyItemId, ownerId, createDate, word, definition) VALUES( ?, ?, ?, ? , ?)";
			PreparedStatement stmt = conn.prepareStatement(statement);
			stmt.setString(1, item.getId());
			stmt.setString(2, getHackedUserId(item.getOwner()));
			stmt.setTimestamp(3, new java.sql.Timestamp(item.getCreateDate().getTime()));
			stmt.setString(4, item.getWord());
			stmt.setString(5, item.getDefinition());
			int success = 2;
			success = stmt.executeUpdate();
			if (success == 0) {
				LOG.severe("Failed to insert StudyItem");
			}

		} finally {
			// conn.close();
		}

	}

	public void insert(Connection conn, StudyItemMeta item) throws SQLException {
		String statement = "INSERT INTO study_item_meta (studyItemMetaId, createDate, ownerId, studyItemId,incorrectCount,viewCount,lastUpdate,"
				+ "difficulty, confidence, totalTime, averageTime, timedViewCount) VALUES( ?, ?, ?,?, ?, ?,?, ?, ?, ?, ?, ?)";
		PreparedStatement stmt = conn.prepareStatement(statement);
		UUID uuid = java.util.UUID.randomUUID();
		stmt.setString(1, uuid.toString());
		stmt.setTimestamp(2, new java.sql.Timestamp(item.getCreateDate().getTime()));
		stmt.setString(3, getHackedUserId(item.getOwner()));
		stmt.setString(4, item.getStudyItemId());
		stmt.setLong(5, item.getIncorrectCount());
		stmt.setLong(6, item.getViewCount());
		if(item.getLastUpdate() != null){
			stmt.setTimestamp(7, new java.sql.Timestamp(item.getLastUpdate().getTime()));
		}
		else {
			stmt.setTimestamp(7, null);
		}
		stmt.setDouble(8, item.getDifficulty());
		stmt.setDouble(9, item.getConfidence());
		stmt.setLong(10, item.getTotalTime());
		stmt.setLong(11, item.getAverageTime());
		stmt.setLong(12, item.getTimedViewCount());

		int success = 2;
		success = stmt.executeUpdate();
		if (success == 0) {
			LOG.severe("Failed to insert study_item_meta");
		}
	}

	public void insert(Connection conn, PersistantTagName item)
			throws SQLException {
		String statement = "INSERT INTO tag (tagId, tagName, ownerId) VALUES( ?, ?, ?)";
		PreparedStatement stmt = conn.prepareStatement(statement);
		stmt.setString(1, item.getTagId());
		stmt.setString(2, item.getTagName());
		stmt.setString(3, getHackedUserId(item.getUser().getEmail()));

		int success = 2;
		success = stmt.executeUpdate();
		if (success == 0) {
			LOG.severe("Failed to insert tag");
		}
	}
}
