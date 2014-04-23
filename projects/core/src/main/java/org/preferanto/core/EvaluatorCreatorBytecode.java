package org.preferanto.core;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvaluatorCreatorBytecode {
	private static final Logger log = LoggerFactory.getLogger(EvaluatorCreatorBytecode.class);
    private static final Method defineClassMethod;
    static {
        Method m;
        try {
            m = ClassLoader.class.getDeclaredMethod("defineClass", new Class[] {String.class, byte[].class, Integer.TYPE, Integer.TYPE}); 
            m.setAccessible(true); 
        } catch (NoSuchMethodException nsme) {
            m = null;
        }
        defineClassMethod = m;
    }

	@SuppressWarnings("unchecked")
	public static PreferantoEvaluator createFrom(PreferantoCompiler compiler) throws InstantiationException, IllegalAccessException {
		String evalName = compiler.getPrefEvalClassNameDot();
		String objectivesName = compiler.getPrefObjectivesClassNameDot();
		
		byte[] evalByteCode = compiler.getEvalByteCode();
		byte[] objectivesByteCode = compiler.getObjectivesByteCode();
		
		Class<PreferantoEvaluator> evalClass;
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			defineClassMethod.invoke(cl, new Object[]{objectivesName, objectivesByteCode, 0, objectivesByteCode.length});
			evalClass = (Class<PreferantoEvaluator>)defineClassMethod.invoke(cl, new Object[]{evalName, evalByteCode, 0, evalByteCode.length});
		} catch(Throwable e) {
			log.error("Failed to create class " + evalName, e);
			throw new RuntimeException(e);
		}
		PreferantoEvaluator evaluator = evalClass.newInstance();
		return evaluator;
	}

}
