package org.ttdc.flipcards.shared;

import java.io.Serializable;
import java.util.List;


public class AutoCompleteWordPairList implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4134463557586575411L;
	private List<WordPair> wordPairs;
	private int sequence;
	
	public AutoCompleteWordPairList(int sequence, List<WordPair> wordPairs) {
		this.wordPairs = wordPairs;
		this.sequence = sequence;
	}
	
	public AutoCompleteWordPairList() {
	}

	public List<WordPair> getWordPairs() {
		return wordPairs;
	}

	public void setWordPairs(List<WordPair> wordPairs) {
		this.wordPairs = wordPairs;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	
}	
