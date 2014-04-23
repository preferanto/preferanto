package org.preferanto.core;

public class Goal {
	public Expression rule1;
	public Expression rule2;
	public Direction direction;
	
	@Override
	public String toString() {
		return rule1 + " : " + direction;
	}
}
