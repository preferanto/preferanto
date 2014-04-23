package org.preferanto.core;

import java.util.ArrayList;
import java.util.List;

public class Preference {
	public Expression condition;
	public final List<Goal> goals = new ArrayList<Goal>();
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[').append(condition) .append("] ");
		if(goals.size() == 1) {
			sb.append(goals.get(0));
		} else {
			String sep = "<";
			for(Goal goal : goals) {
				sb.append(sep).append(goal);
				sep = ", ";
			}
			sb.append('>');
		}
		return sb.toString();
	}
}
