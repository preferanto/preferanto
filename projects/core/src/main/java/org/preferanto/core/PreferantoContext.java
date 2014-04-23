package org.preferanto.core;

import java.io.Serializable;
import java.util.List;

public interface PreferantoContext extends Serializable {
	void reset(List<QuantitySymbol> quantities);

	String getAsString(QuantitySymbol quantity);
	void setFromString(QuantitySymbol quantity, String value);
	
	long getLong(String name);
	void setLong(String name, long value);

	double getDouble(String name);
	void setDouble(String name, double value);

	boolean getBoolean(String name);
	void setBoolean(String name, boolean value);

	String getString(String name);
	void setString(String name, String value);
}
