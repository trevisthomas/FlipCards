package org.ttdc.flipcards.shared;

public enum ItemFilter {
	BOTH("All"), 
	ACTIVE("Active Only"), 
	INACTIVE("Inactive Only"), ;

	private final String name;

	ItemFilter(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
