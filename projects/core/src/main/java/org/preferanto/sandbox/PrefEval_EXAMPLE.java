package org.preferanto.sandbox;

import static org.preferanto.core.Utils.*;
import org.preferanto.core.PreferantoEvaluator;
import org.preferanto.core.PreferantoContext;

public class PrefEval_EXAMPLE implements PreferantoEvaluator {
	@Override
	public int getRuleCount() {
		return 4;
	}

	@Override
	public int getConditionalRuleCount() {
		return 3;
	}

	@Override
	public double[] compare(int ruleIndex, PreferantoContext ctx1, PreferantoContext ctx2) {
		PreferantoObjectives_EXAMPLE obj1 = new PreferantoObjectives_EXAMPLE(ctx1);
		PreferantoObjectives_EXAMPLE obj2 = new PreferantoObjectives_EXAMPLE(ctx2);
		switch(ruleIndex) {
			case 0:
				return compare0(obj1, obj2);
			case 1:
				return compare1(obj1, obj2);
			case 2:
				return compare2(obj1, obj2);
			case 3:
				return compare3(obj1, obj2);
		}
		return null;
	}

	private double[] compare0_0(PreferantoObjectives_EXAMPLE obj1, PreferantoObjectives_EXAMPLE obj2) {
		double result[] = new double[2];
		if((obj1.responseTime > 5) ^ (obj2.responseTime > 5)) {
			result[0] = doubleDiff(obj2.responseTime, obj1.responseTime);
			result[1] = doubleDiff(obj2.responseTime, obj1.responseTime);
		}
		return result;
	}

	private double[] compare0(PreferantoObjectives_EXAMPLE obj1, PreferantoObjectives_EXAMPLE obj2) {
		double[] result = null;
		if((obj1.responseTime > 5) ^ (obj2.responseTime > 5)) {
			double result0 = doubleDiff(obj2.responseTime, obj1.responseTime);
			double result1 = doubleDiff(obj2.responseTime, obj1.responseTime);
			result = new double[] {result0, result1};
		}
		return result;
	}

	private double[] compare1(PreferantoObjectives_EXAMPLE obj1, PreferantoObjectives_EXAMPLE obj2) {
		double[] result = new double[1];
		if(Math.abs(obj1.cost - obj2.cost) > 2) {
			result[0] = doubleDiff(obj2.cost, obj1.cost);
		}
		return result;
	}

	private double[] compare2(PreferantoObjectives_EXAMPLE obj1, PreferantoObjectives_EXAMPLE obj2) {
		double[] result = new double[1];
		if((obj1.colors < 65536) || (obj2.colors < 65536)) {
			result[0] = longDiff(obj1.colors, obj2.colors);
		}
		return result;
	}

	private double[] compare3(PreferantoObjectives_EXAMPLE obj1, PreferantoObjectives_EXAMPLE obj2) {
		double[] result = new double[1];
		result[0] = doubleDiff(obj2.responseTime, obj1.responseTime);
		return result;
	}

	@Override
	public boolean isSatisfyingConstraints(PreferantoContext context) {
		return true;
	}
}