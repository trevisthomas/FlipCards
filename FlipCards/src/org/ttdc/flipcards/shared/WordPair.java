package org.ttdc.flipcards.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WordPair implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3347383363425803403L;
	
	private String word;
	private String definition;
	private Date createDate = new Date();
	private Date lastUpdate = new Date();
	private long testedCount;
	private long correctCount;
	private double difficulty;
	private String id;
	private String user;
	private long displayOrder;
	private List<Tag> tags = new ArrayList<>();
	private boolean active = false;
	private boolean deleteAllowed;
	private long averageTime;

	public WordPair() {

	}

	public String getId() {
		return id;
	}

	public WordPair(String id, String word, String definition) {
		this.id = id;
		this.word = word;
		this.definition = definition;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tagNameIds) {
		this.tags = tagNameIds;
	}

	public String getWord() {
		return word;
	}

	public String getDefinition() {
		return definition;
	}

	public long getTestedCount() {
		return testedCount;
	}

	public void setTestedCount(long testedCount) {
		this.testedCount = testedCount;
	}

	public long getCorrectCount() {
		return correctCount;
	}

	public void setCorrectCount(long correctCount) {
		this.correctCount = correctCount;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public long getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(long displayOrder) {
		this.displayOrder = displayOrder;
	}
	
	public double getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(double difficulty) {
		this.difficulty = difficulty;
	}
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	public boolean isDeleteAllowed() {
		return deleteAllowed;
	}

	public void setDeleteAllowed(boolean deleteAllowed) {
		this.deleteAllowed = deleteAllowed;
	}
	
	public long getAverageTime() {
		return averageTime;
	}

	public void setAverageTime(long averageTime) {
		this.averageTime = averageTime;
	}
	
	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (active ? 1231 : 1237);
		result = prime * result + (int) (correctCount ^ (correctCount >>> 32));
		result = prime * result
				+ ((createDate == null) ? 0 : createDate.hashCode());
		result = prime * result
				+ ((definition == null) ? 0 : definition.hashCode());
		long temp;
		temp = Double.doubleToLongBits(difficulty);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (displayOrder ^ (displayOrder >>> 32));
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
		result = prime * result + (int) (testedCount ^ (testedCount >>> 32));
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
		WordPair other = (WordPair) obj;
		if (active != other.active)
			return false;
		if (correctCount != other.correctCount)
			return false;
		if (createDate == null) {
			if (other.createDate != null)
				return false;
		} else if (!createDate.equals(other.createDate))
			return false;
		if (definition == null) {
			if (other.definition != null)
				return false;
		} else if (!definition.equals(other.definition))
			return false;
		if (Double.doubleToLongBits(difficulty) != Double
				.doubleToLongBits(other.difficulty))
			return false;
		if (displayOrder != other.displayOrder)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (tags == null) {
			if (other.tags != null)
				return false;
		} else if (!tags.equals(other.tags))
			return false;
		if (testedCount != other.testedCount)
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
