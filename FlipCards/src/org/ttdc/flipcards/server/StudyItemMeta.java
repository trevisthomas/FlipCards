package org.ttdc.flipcards.server;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.ttdc.flipcards.shared.WordPair;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class StudyItemMeta implements Serializable{
//	@PrimaryKey
//	@Persistent
//	@Index(name="STAGING_CARD_ID_IDX")
//	private String id;
	
	

	public static class SortDifficulty implements Comparator<WordPair> {
		public int compare(WordPair c1, WordPair c2) {
			return new Double(c1.getDifficulty()).compareTo(c2.getDifficulty());
		}
	}

	public static class SortDifficultyDesc implements Comparator<WordPair> {
		public int compare(WordPair c1, WordPair c2) {
			return new Double(c2.getDifficulty()).compareTo(c1.getDifficulty());
		}
	}
	
	public static class SortAlphabetical implements Comparator<WordPair> {
		public int compare(WordPair c1, WordPair c2) {
			return c1.getWord().compareTo(c2.getWord());
		}
	}
	
	public static class SortCreateDate implements Comparator<WordPair> {
		public int compare(WordPair c1, WordPair c2) {
			return c1.getCreateDate().compareTo(c2.getCreateDate());
		}
	}
	
	public static class SortCreateDateDesc  implements Comparator<WordPair> {
		public int compare(WordPair c1, WordPair c2) {
			return c2.getCreateDate().compareTo(c1.getCreateDate());
		}
	}
	
	public static class SortStudyCount implements Comparator<WordPair> {
		public int compare(WordPair c1, WordPair c2) {
			return new Long(c1.getTestedCount()).compareTo(c2.getTestedCount());
		}
	}
	
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
	@Persistent
	private Date createDate = new Date();
	@Persistent
	private String owner;
	@Persistent
	@Index(name="STUDY_ITEM_META_ID_IDX")
	private String studyItemId; //Not unique
	@Persistent
	private Long incorrectCount = 0L;
	@Persistent
	private Long viewCount = 0L;
	@Persistent
	private Date lastUpdate;
	@Persistent
	private Double difficulty = 1.0; // New words are infinitely difficult.
										// Difficulty is a number between 0 and
										// 1. 1 is hardest, 0 is easiest
	@Persistent
	private Double confidence = 0.0; // Average percentage of quiz total time on
										// this card. The theory being that if
										// you think long, you're not very
										// confident. A higher number is less
										// good. Values range from 0 - 1, 0 is
										// total confidence. 1 is infinitely
										// little confidence
	public Key getKey() {
		return key;
	}
	public void setKey(Key key) {
		this.key = key;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getStudyItemId() {
		return studyItemId;
	}
	public void setStudyItemId(String studyItemId) {
		this.studyItemId = studyItemId;
	}
	public Long getIncorrectCount() {
		return incorrectCount;
	}
	public void setIncorrectCount(Long incorrectCount) {
		this.incorrectCount = incorrectCount;
	}
	public Long getViewCount() {
		return viewCount;
	}
	public void setViewCount(Long viewCount) {
		this.viewCount = viewCount;
	}
	public Date getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public Double getDifficulty() {
		return difficulty;
	}
	public void setDifficulty(Double difficulty) {
		this.difficulty = difficulty;
	}
	public Double getConfidence() {
		return confidence;
	}
	public void setConfidence(Double confidence) {
		this.confidence = confidence;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(" difficulty:").append(difficulty).append(" created:").append(createDate).append(" lastStudied").append(lastUpdate).append(" Counts:").append(incorrectCount).append("/").append(viewCount);
		return builder.toString();
	}
	
}
