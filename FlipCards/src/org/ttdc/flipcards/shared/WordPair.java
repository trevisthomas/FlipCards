package org.ttdc.flipcards.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ttdc.flipcards.server.PersistantWordPair;


public class WordPair implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5138759608971399534L;

	private String word;
	private String definition;
	private Date createDate = new Date();
	private long testedCount;
	private long correctCount;
	private double difficulty;
	private String id;
	private String user;
	private long displayOrder;
	private List<Tag> tags = new ArrayList<>();

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
		WordPair other = (WordPair) obj;
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
