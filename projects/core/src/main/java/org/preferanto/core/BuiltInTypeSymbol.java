package org.preferanto.core;

public class BuiltInTypeSymbol extends Symbol implements Type {
	public static final BuiltInTypeSymbol _BOOLEAN = new BuiltInTypeSymbol("boolean");
	public static final BuiltInTypeSymbol _INTEGER = new BuiltInTypeSymbol("integer");
	public static final BuiltInTypeSymbol _REAL = new BuiltInTypeSymbol("real");
	public static final BuiltInTypeSymbol _STRING = new BuiltInTypeSymbol("string");
	
    public BuiltInTypeSymbol(String name) { 
    	super(name, null); 
    }
    
    @Override
    public String toString() {
    	return getName();
    }
}
