package org.ttdc.flipcards.client;

public enum ViewName {
	QUIZ_SELECTION("Quiz"), 
	CARD_MANAGER("Edit Cards"), 
	QUIZ("Quiz"),
	RESULT("Result"),
	FLIPCARDS("Result"),
	DEBUG("Debug"),;

	private final String name;

	ViewName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
