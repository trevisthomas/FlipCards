package org.ttdc.flipcards.shared;

public enum CardOrder {
	HARDEST("Hardest"),
	SLOWEST("Slowest to answer"),
	EASIEST("Easiest"),
	LEAST_STUDIED("Least Studied"),
	LATEST_ADDED("Most Reciently Added"),
	RANDOM("Random"), LEAST_RECIENTLY_STUDIED("Least Reciently Studied"),
	TERM_DES("Term Descending", false),
	TERM("Term", false),
	;
	
	private final String name;
	private final boolean quizable;
	
	CardOrder(String name){
		this(name, true);
	}
	
	CardOrder(String name, boolean quizable){
		this.name = name;
		this.quizable = quizable;
	}

	public boolean isQuizable() {
		return quizable;
	}

	@Override
	public String toString() {
		return name;
	}
}
