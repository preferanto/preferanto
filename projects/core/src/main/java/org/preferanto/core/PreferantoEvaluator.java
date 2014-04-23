package org.preferanto.core;

public interface PreferantoEvaluator  {
	int getRuleCount();
	int getConditionalRuleCount();
	boolean isSatisfyingConstraints(PreferantoContext context);
	double[] compare(int ruleIndex, PreferantoContext ctx1, PreferantoContext ctx2);
}
