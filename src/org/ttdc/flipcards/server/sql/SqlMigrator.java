package org.ttdc.flipcards.server.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.ttdc.flipcards.server.PersistantTagName;
import org.ttdc.flipcards.server.StudyItem;
import org.ttdc.flipcards.server.StudyItemMeta;
import org.ttdc.flipcards.server.StudyWordsServiceImpl;
import org.ttdc.flipcards.server.TagAssociation;

public class SqlMigrator {
	private static final Logger LOG = Logger.getLogger(SqlMigrator.class
			.getName());

	private final static int pageSize = 20;
	
	public static boolean migrate(int table, int pageNumber){
		SqlMigrator migrator = new SqlMigrator();
		if(table == 0){
			return migrator.migrateTagAsses(pageNumber);
		} else if(table == 1){
			return migrator.migrateStudyWords(pageNumber);
		} else if(table == 2){
			return migrator.migrateMetaItem(pageNumber);
		} else if(table == 3){
			return migrator.migrateTags(pageNumber);
		}
		
		
		throw new IllegalArgumentException("Table index is bad");
	}

	
	public boolean migrateTagAsses(int pageNumber) {
		SqlDaoHelper sql = new SqlDaoHelper();
		PersistenceManager pm = StudyWordsServiceImpl.getPersistenceManager();
		List<TagAssociation> tagAsses = new ArrayList<>();
		Connection sqlConnection = null;
		
		try {
			sqlConnection = sql.getConnection();
			Query q = pm.newQuery(TagAssociation.class);
//			q.setOrdering("key desc");
//			q.setRange(offset, pageSize);
			
			int start = (pageNumber - 1) * pageSize;
			int end = start + pageSize;
			q.setRange(start, end);
			
			tagAsses = (List<TagAssociation>) q.execute();
			for (TagAssociation tagAss : tagAsses) {
				sql.insert(sqlConnection,tagAss);
			}
			return tagAsses.size() != pageSize;

		} catch (Exception e) {
			LOG.severe(e.getMessage());
			throw new IllegalArgumentException(
					"Failed to retrieve tag associations", e);
		} finally {
			pm.close();
			if(sqlConnection != null){
				try {
					sqlConnection.close();
				} catch (SQLException e) {
					LOG.severe(e.getMessage());
					throw new IllegalArgumentException(
							"Failed to retrieve tag associations", e);
				}
			}
		}
	}
	
	
	private boolean migrateStudyWords(int pageNumber) {
		SqlDaoHelper sql = new SqlDaoHelper();
		PersistenceManager pm = StudyWordsServiceImpl.getPersistenceManager();
		List<StudyItem> items = new ArrayList<>();
		Connection sqlConnection = null;
		
		try {
			sqlConnection = sql.getConnection();
			Query q = pm.newQuery(StudyItem.class);
			
			int start = (pageNumber - 1) * pageSize;
			int end = start + pageSize;
			q.setRange(start, end);
			
			items = (List<StudyItem>) q.execute();
			for (StudyItem item : items) {
				sql.insert(sqlConnection,item);
			}
			return items.size() != pageSize;

		} catch (Exception e) {
			LOG.severe(e.getMessage());
			throw new IllegalArgumentException(
					"Failed to retrieve study items", e);
		} finally {
			pm.close();
			if(sqlConnection != null){
				try {
					sqlConnection.close();
				} catch (SQLException e) {
					LOG.severe(e.getMessage());
					throw new IllegalArgumentException(
							"Failed to close study items data store", e);
				}
			}
		}
	}
	
	private boolean migrateMetaItem(int pageNumber) {
		SqlDaoHelper sql = new SqlDaoHelper();
		PersistenceManager pm = StudyWordsServiceImpl.getPersistenceManager();
		List<StudyItemMeta> items = new ArrayList<>();
		Connection sqlConnection = null;
		
		try {
			sqlConnection = sql.getConnection();
			Query q = pm.newQuery(StudyItemMeta.class);
			
			int start = (pageNumber - 1) * pageSize;
			int end = start + pageSize;
			q.setRange(start, end);
			
			items = (List<StudyItemMeta>) q.execute();
			for (StudyItemMeta item : items) {
				sql.insert(sqlConnection,item);
			}
			return items.size() != pageSize;

		} catch (Exception e) {
			LOG.severe(e.getMessage());
			throw new IllegalArgumentException(
					"Failed to retrieve StudyItemMeta", e);
		} finally {
			pm.close();
			if(sqlConnection != null){
				try {
					sqlConnection.close();
				} catch (SQLException e) {
					LOG.severe(e.getMessage());
					throw new IllegalArgumentException(
							"Failed to close StudyItemMeta data store", e);
				}
			}
		}
	}
	
	private boolean migrateTags(int pageNumber) {
		SqlDaoHelper sql = new SqlDaoHelper();
		PersistenceManager pm = StudyWordsServiceImpl.getPersistenceManager();
		List<PersistantTagName> items = new ArrayList<>();
		Connection sqlConnection = null;
		
		try {
			sqlConnection = sql.getConnection();
			Query q = pm.newQuery(PersistantTagName.class);
			
			int start = (pageNumber - 1) * pageSize;
			int end = start + pageSize;
			q.setRange(start, end);
			
			items = (List<PersistantTagName>) q.execute();
			for (PersistantTagName item : items) {
				sql.insert(sqlConnection,item);
			}
			return items.size() != pageSize;

		} catch (Exception e) {
			LOG.severe(e.getMessage());
			throw new IllegalArgumentException(
					"Failed to retrieve PersistantTagName", e);
		} finally {
			pm.close();
			if(sqlConnection != null){
				try {
					sqlConnection.close();
				} catch (SQLException e) {
					LOG.severe(e.getMessage());
					throw new IllegalArgumentException(
							"Failed to close PersistantTagName data store", e);
				}
			}
		}
	}
	
//	public void migrateTagAsses() {
//		SqlDaoHelper sql = new SqlDaoHelper();
//		PersistenceManager pm = StudyWordsServiceImpl.getPersistenceManager();
//		List<TagAssociation> tagAsses = new ArrayList<>();
//		Connection sqlConnection = null;
//		
//		try {
//			sqlConnection = sql.getConnection();
//			Query q = pm.newQuery(TagAssociation.class);
//			q.setOrdering("key desc");
//			q.getFetchPlan().setFetchSize(100); 
//			tagAsses = (List<TagAssociation>) q.execute();
//			for (TagAssociation tagAss : tagAsses) {
//				sql.insert(sqlConnection,tagAss);
//			}
//
//		} catch (Exception e) {
//			LOG.severe(e.getMessage());
//			throw new IllegalArgumentException(
//					"Failed to retrieve tag associations", e);
//		} finally {
//			pm.close();
//			if(sqlConnection != null){
//				try {
//					sqlConnection.close();
//				} catch (SQLException e) {
//					LOG.severe(e.getMessage());
//					throw new IllegalArgumentException(
//							"Failed to retrieve tag associations", e);
//				}
//			}
//		}
//
//	}
}
