package org.preferanto.experiment.selectivity;

import java.util.ArrayList;
import java.util.List;

import org.jgap.FitnessFunction;
import org.jgap.IChromosome;
import org.preferanto.core.PreferantoContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectivityFitnessFunction extends FitnessFunction {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(SelectivityFitnessFunction.class);

	private final SelectivityProvider selectivityProvider;
	private final int objCount;
	private final int ruleCount;
	private final int tupleSize;
	private final int termCount;
	private final double desiredSelectivity;
	
	private final List<List<PreferantoContext>> contextLists = new ArrayList<>();
	
	public SelectivityFitnessFunction(SelectivityProvider selectivityProvider, double desiredSelectivity, 
			int objCount, int ruleCount, int tupleSize, int termCount, int paretoSize, int maxRuns) {
		this.selectivityProvider = selectivityProvider;
		this.desiredSelectivity = desiredSelectivity;
		this.objCount = objCount;
		this.ruleCount = ruleCount;
		this.tupleSize = tupleSize;
		this.termCount = termCount;
		
		long startTime = System.currentTimeMillis();
		ParetoContextProvider ctxProvider = new DTLZ2RndParetoContextProvider(objCount);
		for(int i=0; i<maxRuns; i++) {
			List<PreferantoContext> contexts = new ArrayList<>();
			for(int k=0; k<paretoSize; k++) {
				contexts.add(ctxProvider.getNextContext());				
			}
			contextLists.add(contexts);
		}
		if(log.isTraceEnabled()) {
			long duration = System.currentTimeMillis() - startTime;
			log.trace("contextLists created in " + duration + " ms.");
		}
	}

	@Override
	protected double evaluate(IChromosome chromosome) {
		PreferantoSolution sol = new PreferantoSolution(termCount, chromosome);
		SelectivityCalculator selectivityCalculator = new SelectivityCalculator(selectivityProvider, objCount, ruleCount, tupleSize, sol.getAlphas());
		AggregatedSelectivity aggregatedSelectivity = selectivityCalculator.getAggregatedSelectivity(contextLists);
		
		chromosome.setApplicationData(aggregatedSelectivity);
		
		double selectivity = aggregatedSelectivity.getSelectivity();
		double fitness = 1 - Math.abs(desiredSelectivity - selectivity);
		return fitness;
	}

}
