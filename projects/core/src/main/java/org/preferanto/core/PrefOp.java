package org.preferanto.core;

public enum PrefOp {
	UNSPECIFIED(null), AT_LEAST_ONE(""), ALL("~"), EXACTLY_ONE("#"), DIFF("@");
	
	public final String symbol;

	PrefOp(String symbol) {
		this.symbol = symbol;
	}
	
	public static PrefOp fromName(String name) {
		for(PrefOp prefOp : values()) {
			if(name.equals(prefOp.name())) return prefOp;
			if(name.equals(prefOp.symbol)) return prefOp;
		}
		return null;
	}
	
}
