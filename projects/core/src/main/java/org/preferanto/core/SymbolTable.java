package org.preferanto.core;

import java.util.HashMap;
import java.util.Map;

import static org.preferanto.core.BuiltInTypeSymbol.*;

public class SymbolTable {
	private final Map<String, Symbol> symbols = new HashMap<String, Symbol>();

    public SymbolTable() { 
    	putBuiltInType(_BOOLEAN);
    	putBuiltInType(_REAL);
    	putBuiltInType(_INTEGER);
    	putBuiltInType(_STRING);
    }
    
    private void putBuiltInType(BuiltInTypeSymbol symbol) {
    	symbols.put(symbol.getName(), symbol);
    }
    
    public Symbol getSymbol(String name) {
    	return symbols.get(name);
    }
 
    public void putSymbol(Symbol symbol) {
    	Symbol oldSymbol = symbols.put(symbol.getName(), symbol);
    	if(oldSymbol != null) {
    		throw new RuntimeException("Symbol already defined: " + symbol.getName() + " (" + oldSymbol + " / " + symbol + ")");
    	}
    }
    
    @Override
    public String toString() { 
    	return symbols.toString(); 
    }
}
