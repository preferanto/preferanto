package org.preferanto.experiment.moea;

import java.util.Map;

import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.DominanceComparator;
import org.preferanto.poset.Poset;

public class PreferantoComparator implements DominanceComparator {
	// maps solution ids to their corresponding index in the poset matrix
	private final Map<Long, Integer> solutionIndexes;
	private final Poset poset;
	
	
	public PreferantoComparator(Map<Long, Integer> solutionIndexes, Poset poset) {
		this.solutionIndexes = solutionIndexes;
		this.poset = poset;
	}

	@Override
	public int compare(Solution solution1, Solution solution2) {
		Integer idx1 = solutionIndexes.get(solution1.getId());
		Integer idx2 = solutionIndexes.get(solution2.getId());
		return -poset.getRule(idx1, idx2);
	}

}
