package org.ttdc.flipcards.shared;

import java.io.Serializable;
import java.util.Date;
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
	
//	public WordPair(String word, String definition) {
//		this.id = id;
//		this.word = word;
//		this.definition = definition;
//	}

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
}
