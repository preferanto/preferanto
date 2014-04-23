package org.preferanto.core;

import static org.preferanto.core.BuiltInTypeSymbol.*;

public class Expression {
	public static final int FIRST = 1;
	public static final int SECOND = 2;
	
	public int arg = 0;
	public Type type = null;
	public PrefOp prefOp = null;

	public Expression() {
	}
	
	public Expression(Expression expr) {
		if(expr != null) {
			this.arg = expr.arg;
			this.type = expr.type;
			this.prefOp = expr.prefOp;
		}
	}
	
	@Override
	public String toString() {		
		return arg + ": " + type + "/" + prefOp;
	}
	
	public static Type getResultType(Type type1, Type type2) throws PreferantoPredicateException {
		Type resultType = _INTEGER;
		if(type1 == _REAL) {
			resultType = _REAL;
		} else if(type1 != _INTEGER) {
			error("getResultType", "Invalid type in arithmetic expression: " + type1);
		}
		if(type2 == _REAL) {
			resultType = _REAL;
		} else if(type2 != _INTEGER) {
			error("getResultType", "Invalid type in arithmetic expression: " + type2);
		}
		return resultType;
	}
	
	public static PrefOp getResultPrefOpMath(PrefOp prefOp1, PrefOp prefOp2) throws PreferantoPredicateException {
		PrefOp resultPrefOp = null;
		if(prefOp1 != null && prefOp1 != PrefOp.DIFF) error("getResultPrefOpMath", "Invalid preference operator for arithmetic expression: " + prefOp1);
		if(prefOp2 != null && prefOp2 != PrefOp.DIFF) error("getResultPrefOpMath", "Invalid preference operator for arithmetic expression: " + prefOp2);
		if(prefOp1 == PrefOp.DIFF || prefOp2 == PrefOp.DIFF) resultPrefOp = PrefOp.DIFF;
		return resultPrefOp;
	}

	public static PrefOp getResultPrefOpBool(PrefOp prefOp1, PrefOp prefOp2) {
		PrefOp resultPrefOp = ((prefOp1 != null) || (prefOp2 != null)) ? PrefOp.UNSPECIFIED : null;
		return resultPrefOp;
	}
	
	public static void checkPrefOp(PrefOp prefOp, Expression expr) throws PreferantoPredicateException {
		if(expr.prefOp != null) error("checkPrefOp", "Preference operator already set");
		if(prefOp == PrefOp.DIFF && !isNumeric(expr.type)) error("checkPrefOp", "Expected numeric type");
		if(prefOp != PrefOp.DIFF && expr.type != _BOOLEAN) error("checkPrefOp", "Expected boolean type");
	}
	
	public static boolean isNumeric(Type type) {
		return (type == _REAL || type == _INTEGER);
	}
	
	public static void checkNumeric(Type type) throws PreferantoPredicateException {
		if(type != _REAL && type != _INTEGER) error("checkNumeric", "Expected numeric type, but was: " + type);
	}
	
	public static void checkBoolean(Type type) throws PreferantoPredicateException {
		if(type != _BOOLEAN) error("checkBoolean", "Expected boolean type, but was: " + type);
	}
	
	public static void error(String ruleName, String predicateText) throws PreferantoPredicateException {
		PreferantoPredicateException exc;
		try {
			exc = new PreferantoPredicateException(ruleName, predicateText);
		} catch(Exception e) {
			throw new RuntimeException(ruleName + ": " + predicateText);
		}
		throw exc;
	}
}
