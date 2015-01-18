package org.ttdc.flipcards.shared;

public enum CardSide {
	TERM("Term"), 
	DEFINITION("Definition"), 
	RANDOM("Random"), ;

	private final String name;

	CardSide(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
