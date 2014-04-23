package org.moeaframework.core.comparator;

import org.moeaframework.core.Population;

public interface DominanceComparatorProvider {
	DominanceComparator getComparator(Population population);
	boolean isPopulationDependent();
}
