/* Copyright 2009-2013 David Hadka
 *
 * This file is part of the MOEA Framework.
 *
 * The MOEA Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * The MOEA Framework is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the MOEA Framework.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.moeaframework.core.comparator;

import java.io.Serializable;

import org.moeaframework.core.Population;

public class ChainedComparatorProvider implements DominanceComparatorProvider, Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * The comparatorProviders in the order they are to be applied.
	 */
	private final DominanceComparatorProvider[] comparatorProviders;
	private final boolean populationDependent;

	public ChainedComparatorProvider(DominanceComparatorProvider... comparatorProviders) {
		super();
		this.comparatorProviders = comparatorProviders;
		boolean popDep = false;
		for(DominanceComparatorProvider provider : comparatorProviders) {
			if(provider.isPopulationDependent()) {
				popDep = true;
				break;
			}
		}
		populationDependent = popDep;
	}

	@Override
	public DominanceComparator getComparator(Population population) {
		DominanceComparator[] comparators = new DominanceComparator[comparatorProviders.length];
		for(int i=0; i<comparatorProviders.length; i++) {
			comparators[i] = comparatorProviders[i].getComparator(population);
		}
		return new ChainedComparator(comparators);
	}

	@Override
	public boolean isPopulationDependent() {
		return populationDependent;
	}
	
}
