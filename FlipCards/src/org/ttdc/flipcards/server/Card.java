package org.ttdc.flipcards.server;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.users.User;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Card implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5053123430650344749L;
	

	@Persistent
	private String word;
	@Persistent
	private String definition;
	@Persistent
	private Date createDate = new Date();
	@Persistent
	private User user;
	@Persistent
	private String dictionaryId;
	@PrimaryKey
	@Persistent
	private String id;
	@Persistent
	private Long incorrectCount = 0L;
	@Persistent
	private Long viewCount = 0L;
	@Persistent
	private Date lastUpdate;
	@Persistent
	private Double difficulty = 1.0; //New words are infinitely difficult.  Difficulty is a number between 0 and 1. 1 is hardest, 0 is easiest
	@Persistent
	private Double confidence = 0.0; //Average percentage of quiz total time on this card. The theory being that if you think long, you're not very confident.  A higher number is less good.  Values range from 0 - 1, 0 is total confidence.  1 is infinitely little confidence

	public Card() {
		// TODO Auto-generated constructor stub
	}
	
	public Card(String id, String word, String definition,
			String dictionaryId, User user) {
		this.id = id;
		this.word = word;
		this.definition = definition;
		this.dictionaryId = dictionaryId;
		this.user = user;
	}
	

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getDictionaryId() {
		return dictionaryId;
	}

	public void setDictionaryId(String dictionaryId) {
		this.dictionaryId = dictionaryId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public Date getCreateDate() {
		return createDate;
	}
	
	
}
