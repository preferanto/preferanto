package org.preferanto.core;

public enum Direction {
	LOW("low"), HIGH("high");
	
	public final String name;

	Direction(String name) {
		this.name = name;
	}
	
	public static Direction fromName(String name) {
		for(Direction prefOp : values()) {
			if(prefOp.name.equals(name)) return prefOp;
		}
		return null;
	}
	
}
