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
	private int testedCount;
	private int correctCount;
	private String id;
	private String user;
	private int displayOrder;
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

	public int getTestedCount() {
		return testedCount;
	}

	public void setTestedCount(int testedCount) {
		this.testedCount = testedCount;
	}

	public int getCorrectCount() {
		return correctCount;
	}

	public void setCorrectCount(int correctCount) {
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

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}
	
}
