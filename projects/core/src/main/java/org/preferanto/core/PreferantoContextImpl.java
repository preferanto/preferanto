package org.preferanto.core;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PreferantoContextImpl implements PreferantoContext {
	private static final long serialVersionUID = 1L;

	private final Map<String, Long> longValues = new TreeMap<String, Long>();
	private final Map<String, Double> doubleValues = new TreeMap<String, Double>();
	private final Map<String, Boolean> booleanValues = new TreeMap<String, Boolean>();
	private final Map<String, String> stringValues = new TreeMap<String, String>();
	

	@Override
	public void reset(List<QuantitySymbol> quantities) {
		longValues.clear();
		doubleValues.clear();
		booleanValues.clear();
		stringValues.clear();
		for(QuantitySymbol quantity : quantities) {
			Type type = quantity.getType();
			String name = quantity.getName();
			if(type == BuiltInTypeSymbol._BOOLEAN) {
				setBoolean(name, false);
			} else if(type == BuiltInTypeSymbol._REAL) {
				setDouble(name, 0);
			} else if(type == BuiltInTypeSymbol._INTEGER) {
				setLong(name, 0);
			} else {
				setString(name, "");
			}
		}		
	}

	@Override
	public long getLong(String name) {
		Long value = longValues.get(name);
		if(value == null) {
			throw new PreferantoException("Uninitialized long quantity: " + name);
		}
		return value;
	}

	@Override
	public void setLong(String name, long value) {
		longValues.put(name, value);
	}
	
	@Override
	public double getDouble(String name) {
		Double value = doubleValues.get(name);
		if(value == null) {
			throw new PreferantoException("Uninitialized double quantity: " + name);
		}
		return value;
	}

	@Override
	public void setDouble(String name, double value) {
		doubleValues.put(name, value);
	}
	
	@Override
	public boolean getBoolean(String name) {
		Boolean value = booleanValues.get(name);
		if(value == null) {
			throw new PreferantoException("Uninitialized boolean quantity: " + name);
		}
		return value;
	}

	@Override
	public void setBoolean(String name, boolean value) {
		booleanValues.put(name, value);
	}
	
	@Override
	public String getString(String name) {
		String value = stringValues.get(name);
		if(value == null) {
			throw new PreferantoException("Uninitialized string quantity: " + name);
		}
		return value;
	}

	@Override
	public void setString(String name, String value) {
		stringValues.put(name, value);
	}
	
	@Override
	public String getAsString(QuantitySymbol quantity) {
		Type type = quantity.getType();
		String name = quantity.getName();
		if(type == BuiltInTypeSymbol._BOOLEAN) {
			return "" + getBoolean(name);
		} else if(type == BuiltInTypeSymbol._REAL) {
			return "" + getDouble(name);
		} else if(type == BuiltInTypeSymbol._INTEGER) {
			return "" + getLong(name);
		} else {
			return getString(name);
		}
	}

	@Override
	public void setFromString(QuantitySymbol quantity, String value) {
		Type type = quantity.getType();
		String name = quantity.getName();
		if(type == BuiltInTypeSymbol._BOOLEAN) {
			setBoolean(name, Boolean.valueOf(value));
		} else if(type == BuiltInTypeSymbol._REAL) {
			try {
				setDouble(name, Double.parseDouble(value));
			} catch(NumberFormatException e) {
				throw new IllegalArgumentException("Expected a double value for quantity '" + name + "', but was: " + value);
			}
		} else if(type == BuiltInTypeSymbol._INTEGER) {
			try {
				setLong(name, Long.parseLong(value));
			} catch(NumberFormatException e) {
				throw new IllegalArgumentException("Expected a long value for quantity '" + name + "', but was: " + value);
			}
		} else {
			setString(name, value);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb  = new StringBuilder(128);
		sb.append(booleanValues).append(longValues).append(doubleValues).append(stringValues);
		
		
		return sb.toString();
	}
}
