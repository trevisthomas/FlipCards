package org.ttdc.flipcards.shared;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.users.User;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class UserStat {
	@PrimaryKey 
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	@Persistent
	private String wordPairId;
	@Persistent
	private User user;
	@Persistent
	private Long incorrectCount = 0L;
	@Persistent
	private Long viewCount = 0L;
	@Persistent
	private Date dateStamp;
	@Persistent
	private Double difficulty = 1.0; //New words are infinitely difficult.  Difficulty is a number between 0 and 1. 1 is hardest, 0 is easiest
	
	public UserStat(User user, String wordPairId) {
		this.user = user;
		this.wordPairId = wordPairId;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getWordPairId() {
		return wordPairId;
	}
	public void setWordPairId(String wordPairId) {
		this.wordPairId = wordPairId;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
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
	public Date getDateStamp() {
		return dateStamp;
	}
	public void setDateStamp(Date dateStamp) {
		this.dateStamp = dateStamp;
	}
	public Double getDifficulty() {
		return difficulty;
	}
	public void setDifficulty(Double difficulty) {
		this.difficulty = difficulty;
	}
	
	
	
	
}
