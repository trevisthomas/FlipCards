package org.ttdc.flipcards.shared;

import java.io.Serializable;
import java.util.List;

public class PagedWordPair implements Serializable{
	private List<WordPair> wordPair;
	private String cursorString;
	private long totalCardCount = -1;
	private int pageCount = -1;
	
	public PagedWordPair(){}
	
	public PagedWordPair(List<WordPair> wordPair, String cursorString) {
		this.wordPair = wordPair;
		this.cursorString = cursorString;
	}
	
	public List<WordPair> getWordPair() {
		return wordPair;
	}
	public void setWordPair(List<WordPair> wordPair) {
		this.wordPair = wordPair;
	}
	public String getCursorString() {
		return cursorString;
	}
	public void setCursorString(String cursorString) {
		this.cursorString = cursorString;
	}

	public long getTotalCardCount() {
		return totalCardCount;
	}

	public void setTotalCardCount(long totalCardCount) {
		this.totalCardCount = totalCardCount;
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}
	
	
}
