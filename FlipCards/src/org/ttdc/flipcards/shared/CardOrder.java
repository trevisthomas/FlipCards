package org.ttdc.flipcards.shared;

public enum CardOrder {
	HARDEST("Hardest"),
	SLOWEST("Slowest to answer"),
	EASIEST("Easiest"),
	LEAST_STUDIED("Least Studied"),
	LATEST_ADDED("Most Reciently Added"),
	RANDOM("Random"), LEAST_RECIENTLY_STUDIED("Least Reciently Studied"),
	;
	
	private final String name;
	
	CardOrder(String name){
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
