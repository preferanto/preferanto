package org.preferanto.experiment.moea;

import static org.preferanto.experiment.moea.PreferantoAttributes.CONTEXT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.DominanceComparatorProvider;
import org.preferanto.core.PreferantoContext;
import org.preferanto.core.PreferantoContextImpl;
import org.preferanto.poset.Poset;
import org.preferanto.poset.PosetProvider;

public class PreferantoComparatorProvider implements DominanceComparatorProvider {
	private final PosetProvider posetProvider;
	
	public PreferantoComparatorProvider(PosetProvider posetProvider) {
		this.posetProvider = posetProvider;
	}
	
	@Override
	public PreferantoComparator getComparator(Population population) {
		
		
		Map<Long, Integer> solutionIndexes = new HashMap<Long, Integer>();
		List<PreferantoContext> contexts = new ArrayList<>();
		for(int i=0; i<population.size(); i++) {
			Solution solution = population.get(i);
			contexts.add(getContext(solution));
			solutionIndexes.put(solution.getId(), i);
		}
		Poset poset = posetProvider.getPoset(contexts);
		return new PreferantoComparator(solutionIndexes, poset);
	}

	private PreferantoContext getContext(Solution solution) {
		PreferantoContext context;
		Object objContext = solution.getAttribute(CONTEXT);
		if(objContext instanceof PreferantoContext) {
			context = (PreferantoContext) objContext;
		} else {
			context = new PreferantoContextImpl();
			double[] objectives = solution.getObjectives();
			for(int i=0; i<solution.getNumberOfObjectives(); i++) {
				context.setDouble("z" + i, objectives[i]);
			}
			solution.setAttribute(CONTEXT, context);
		}
		return context;
	}
	
	@Override
	public boolean isPopulationDependent() {
		return true;
		// return posetProvider.isPopulationDependent();
	}
}
