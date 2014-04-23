package org.preferanto.experiment.impact;

import org.preferanto.core.PreferantoContext;
import org.preferanto.core.PreferantoEvaluator;
import org.preferanto.experiment.util.ContextListBuilder;

public abstract class PreferenceIndicator {
	private static final double MIN_DIFF = 1.0E-9;
	
	private final PreferantoEvaluator evaluator;
	private final ContextListBuilder ctxBuilder = new ContextListBuilder();

	/**
	 * @param absDiffs - array of (absolute values of) differences between objective values.
	 * @return a value >= 1. If absDiffs contains only 0's, the returned value must be 1.  
	 */
	public abstract double getAggregatedDiff(double[] absDiffs);

	public PreferenceIndicator(PreferantoEvaluator evaluator) {
		this.evaluator = evaluator;
	}
	
	public double getValue(double[] solution1, double[] solution2) {
		if(evaluator.getConditionalRuleCount() > 0) {
			throw new IllegalArgumentException("Only unconditional preferences are allowed.");
		}
		
		double result = 1.0;

		PreferantoContext ctx1 = ctxBuilder.createContext(solution1);
		PreferantoContext ctx2 = ctxBuilder.createContext(solution2);
		
		int ruleCount = evaluator.getRuleCount();
		
		for(int rule = 0; rule < ruleCount; rule++) {
			double[] diffs = evaluator.compare(rule, ctx1, ctx2);
			if(diffs != null) {
				double minDiff = diffs[0];
				double maxDiff = minDiff;
				double[] absDiffs = new double[diffs.length];
				for(int k=0; k<diffs.length; k++) {
					double val = diffs[k];
					if(val < minDiff) {
						minDiff = val;
					}
					if(val > maxDiff) {
						maxDiff = val;
					}
					absDiffs[k] = Math.abs(val);
				}
				if((minDiff < 0) && (maxDiff <= 0) && (Math.abs(minDiff) > MIN_DIFF)) {
					return getAggregatedDiff(absDiffs);									
				} else if((maxDiff > 0) && (minDiff >= 0) && (maxDiff > MIN_DIFF)) {
					return 1 / getAggregatedDiff(absDiffs);									
				}
			}
		}
		
		
		return result;
	}	
}
