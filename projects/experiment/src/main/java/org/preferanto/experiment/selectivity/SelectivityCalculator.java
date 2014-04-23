package org.preferanto.experiment.selectivity;

import java.util.List;

import org.preferanto.core.PreferantoContext;
import org.preferanto.core.PreferantoException;
import org.preferanto.experiment.util.MathUtil;
import org.preferanto.poset.PosetProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectivityCalculator {
	private static final Logger log = LoggerFactory.getLogger(SelectivityCalculator.class);
	private final PosetProvider posetProvider;
	private final SelectivityProvider selectivityProvider;
	private final String preferantoText;
	
	public SelectivityCalculator(SelectivityProvider selectivityProvider, PrefGen prefGen) {
		long startTime = System.currentTimeMillis();
		this.selectivityProvider = selectivityProvider;
		try {
			this.preferantoText = prefGen.getPreferanto();
//			System.out.println("Preferanto specification:\n" + specText);
			this.posetProvider = new PosetProvider(preferantoText);
		} catch(Throwable t) {
			throw new PreferantoException(t);
		}
		if(log.isTraceEnabled()) {
			log.trace("SelectivityCalculator created in " + (System.currentTimeMillis() - startTime) + " ms.");
		}
	}

	public SelectivityCalculator(SelectivityProvider selectivityProvider, int objCount, int ruleCount, int tupleSize, double[] alphas) {
		this(selectivityProvider, new PrefGen(objCount, ruleCount, tupleSize, alphas));
	}
	
	public SelectivityCalculator(SelectivityProvider selectivityProvider, int objCount, int ruleCount, int tupleSize, int termCount, double alpha) {
		this(selectivityProvider, new PrefGen(objCount, ruleCount, tupleSize, termCount, alpha));
	}

	
	public double getSelectivity(List<PreferantoContext> contexts) {
		return selectivityProvider.getSelectivity(contexts, posetProvider);
	}
	
	public AggregatedSelectivity getAggregatedSelectivity(List<List<PreferantoContext>> contextLists) {
		long startTime = System.currentTimeMillis();
		int listCount = contextLists.size();
		double[] selectivities = new double[listCount];
		for(int k=0; k<listCount; k++) {
			double selectivity = getSelectivity(contextLists.get(k));
			selectivities[k] = selectivity;
		}
		long duration = System.currentTimeMillis() - startTime;
		if(log.isTraceEnabled()) {
			log.trace("aggregatedSelectivity computed in " + (System.currentTimeMillis() - startTime) + " ms.");
		}
		return new AggregatedSelectivity(MathUtil.mean(selectivities), MathUtil.stdev(selectivities, false), duration);
	}

	public String getPreferantoText() {
		return preferantoText;
	}
}
