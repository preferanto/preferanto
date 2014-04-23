package org.preferanto.core;

import java.util.ArrayList;
import java.util.List;

public class Specification {
	public final List<QuantitySymbol> quantities = new ArrayList<QuantitySymbol>();
	public final List<Preference> preferences = new ArrayList<Preference>();
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(1024);
		sb.append("*** SPECIFICATION ***");
		sb.append("\nquantities: "); appendList(sb, quantities);
		sb.append("\npreferences: "); appendList(sb, preferences);

		return sb.toString();
	}
	
	private static void appendList(StringBuilder sb, List<?> list) {
		for(Object obj : list) {
			sb.append('\n').append(obj);
		}
	}
}
