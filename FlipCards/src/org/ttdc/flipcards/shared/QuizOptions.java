package org.ttdc.flipcards.shared;

import java.io.Serializable;

public class QuizOptions implements Serializable{
	private String dictionaryId;
	private int size;

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getDictionaryId() {
		return dictionaryId;
	}

	public void setDictionaryId(String dictionaryId) {
		this.dictionaryId = dictionaryId;
	}
	
	
}
