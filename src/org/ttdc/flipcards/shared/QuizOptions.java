package org.ttdc.flipcards.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QuizOptions implements Serializable{
	private CardOrder cardOrder;
	private CardSide cardSide;
	private int size;
	private List<String> tagIds = new ArrayList<>();
	
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public CardOrder getCardOrder() {
		return cardOrder;
	}

	public void setCardOrder(CardOrder cardOrder) {
		this.cardOrder = cardOrder;
	}

	public CardSide getCardSide() {
		return cardSide;
	}

	public void setCardSide(CardSide cardSide) {
		this.cardSide = cardSide;
	}

	public List<String> getTagIds() {
		return tagIds;
	}

	public void setTagIds(List<String> tagIds) {
		this.tagIds = tagIds;
	}
	
	
}
