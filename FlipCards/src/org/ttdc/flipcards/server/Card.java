package org.ttdc.flipcards.server;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.users.User;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Card implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7536447484672984167L;

	public static class CardSortDifficulty implements Comparator<Card> {
		public int compare(Card c1, Card c2) {
			return c1.getDifficulty().compareTo(c2.getDifficulty());
		}
	}

	public static class CardSortDifficultyDesc implements Comparator<Card> {
		public int compare(Card c1, Card c2) {
			return c2.getDifficulty().compareTo(c1.getDifficulty());
		}
	}
	
	public static class CardSortAlphabetical implements Comparator<Card> {
		public int compare(Card c1, Card c2) {
			return c1.getWord().compareTo(c2.getWord());
		}
	}
	
	public static class CardSortCreateDate implements Comparator<Card> {
		public int compare(Card c1, Card c2) {
			return c1.getCreateDate().compareTo(c2.getCreateDate());
		}
	}
	
	public static class CardSortCreateDateDesc  implements Comparator<Card> {
		public int compare(Card c1, Card c2) {
			return c2.getCreateDate().compareTo(c1.getCreateDate());
		}
	}
	
	public static class CardSortStudyCount implements Comparator<Card> {
		public int compare(Card c1, Card c2) {
			return c1.getViewCount().compareTo(c2.getViewCount());
		}
	}

	@Persistent
	private String word;
	@Persistent
	private String definition;
	@Persistent
	private Date createDate = new Date();
	@Persistent
	private User user;
	@PrimaryKey
	@Persistent
	@Index(name="CARD_ID_IDX")
	private String id;
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
			

	public Card() {
		// TODO Auto-generated constructor stub
	}

	public Card(String id, String word, String definition, User user) {
		this.id = id;
		this.word = word;
		this.definition = definition;
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(word).append(":").append(definition).append(" difficulty:").append(difficulty).append(" created:").append(createDate).append(" lastStudied").append(lastUpdate).append(" Counts:").append(incorrectCount).append("/").append(viewCount);
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((definition == null) ? 0 : definition.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Card other = (Card) obj;
		if (definition == null) {
			if (other.definition != null)
				return false;
		} else if (!definition.equals(other.definition))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		if (word == null) {
			if (other.word != null)
				return false;
		} else if (!word.equals(other.word))
			return false;
		return true;
	}
	
	
}
