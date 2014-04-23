package org.preferanto.core;

import org.preferanto.javac.ClassCreator;

public class EvaluatorCreatorSource {
	public static PreferantoEvaluator createFrom(String name, String sourceCode) throws InstantiationException, IllegalAccessException {
		ClassCreator creator = new ClassCreator();
		PreferantoEvaluator prefEvaluator;
		Class<?> cls = creator.createClass(name, sourceCode);
		prefEvaluator = (PreferantoEvaluator)cls.newInstance();
		return prefEvaluator;

	}
}
