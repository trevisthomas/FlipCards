package org.ttdc.flipcards.shared;

import java.io.Serializable;


public class WordPair implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5138759608971399534L;
	
	private String word;
	private String definition;
	private int testedCount;
	private int correctCount;
	private String id; 
	
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
	
	
}
