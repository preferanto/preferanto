package org.preferanto.core;

public abstract class Symbol {
    public final String name;
    public final Type type;

    public Symbol(String name, Type type) { 
    	this.name = name; 
    	this.type = type; 
    }
       
    public String getName() { 
    	return name; 
    }
    
    public Type getType() {
		return type;
	}

    @Override
    public String toString() {
    	String sType = (type == null) ? "" : (": " + type);
        return "<" + getName() + sType + ">";
    }
}
