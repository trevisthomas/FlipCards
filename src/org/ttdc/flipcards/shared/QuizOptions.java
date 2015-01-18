package org.ttdc.flipcards.shared;

import java.io.Serializable;

public class QuizOptions implements Serializable{
	private CardOrder cardOrder;
	private CardSide cardSide;
	private int size;
	
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
}
