package org.moeaframework.core.comparator;

import org.moeaframework.core.Population;

public class PopulationIndependentDominanceComparatorProvider implements DominanceComparatorProvider {
	private final DominanceComparator comparator;
	
	public static final PopulationIndependentDominanceComparatorProvider PARETO_PROVIDER =
			new PopulationIndependentDominanceComparatorProvider(new ParetoDominanceComparator());

	public static final PopulationIndependentDominanceComparatorProvider CROWDING_PROVIDER =
			new PopulationIndependentDominanceComparatorProvider(new CrowdingComparator());

	public static final PopulationIndependentDominanceComparatorProvider RANK_PROVIDER =
			new PopulationIndependentDominanceComparatorProvider(new RankComparator());

	public static final PopulationIndependentDominanceComparatorProvider FITNESS_PROVIDER =
			new PopulationIndependentDominanceComparatorProvider(new FitnessComparator());

	
	public PopulationIndependentDominanceComparatorProvider(DominanceComparator comparator) {
		this.comparator = comparator;
	}
	
	@Override
	public DominanceComparator getComparator(Population population) {
		return comparator;
	}

	@Override
	public boolean isPopulationDependent() {
		return false;
	}

}
