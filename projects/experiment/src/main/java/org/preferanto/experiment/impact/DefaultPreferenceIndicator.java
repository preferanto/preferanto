package org.preferanto.experiment.impact;

import java.util.Arrays;

import org.preferanto.core.PreferantoEvaluator;

public class DefaultPreferenceIndicator extends PreferenceIndicator {

	public DefaultPreferenceIndicator(PreferantoEvaluator evaluator) {
		super(evaluator);
	}

	@Override
	public double getAggregatedDiff(double[] absDiffs) {
		double result = 1;
		for(double diff : absDiffs) {
			if(diff < 0) {
				throw new IllegalArgumentException("Negative value in absDiff: " + Arrays.toString(absDiffs));
			}
			result += diff;
		}
		return result;
	}

}
