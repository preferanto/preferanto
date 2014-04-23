package org.preferanto.experiment.selectivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.preferanto.core.PreferantoContext;

public class DTLZ2SelectivityTest {
	private final SelectivityProvider selectivityProvider;
	private final int objCount;
	private final List<List<PreferantoContext>> contextLists = new ArrayList<>();


	public DTLZ2SelectivityTest(SelectivityProvider selectivityProvider, int objCount, int paretoSize, int maxRuns) {
		this.selectivityProvider = selectivityProvider;
		this.objCount = objCount;

		ParetoContextProvider ctxProvider = new DTLZ2RndParetoContextProvider(objCount);
		for(int i=0; i<maxRuns; i++) {
			List<PreferantoContext> contexts = new ArrayList<>();
			for(int k=0; k<paretoSize; k++) {
				contexts.add(ctxProvider.getNextContext());				
			}
			contextLists.add(contexts);
		}
		
	}

	public AggregatedSelectivity getSelectivity(int ruleCount, int tupleSize, double[] alphas) {
		SelectivityCalculator selectivityCalculator = new SelectivityCalculator(selectivityProvider, objCount, ruleCount, tupleSize, alphas);
		AggregatedSelectivity aggregatedSelectivity = selectivityCalculator.getAggregatedSelectivity(contextLists);
		return aggregatedSelectivity;
	}
	
	public static void main(String[] args) {
//		int objCount = 2;
//		int ruleCount = 1;
//		int tupleSize = 2;
//		double[] alphas = {1.0, 0.5};

		
		int objCount = 4;
		int ruleCount = 3;
		int tupleSize = 3;
		double[] alphas = {1.0, 0.2, 0.6};


		int maxRuns = 10;
		
		System.out.println("ruleCount: " + ruleCount + ", tupleSize: " + tupleSize + ", alphas: " + Arrays.toString(alphas));
		System.out.println("-------------------------------------------------------\n");
		
		
		SelectivityProvider[] providers = {SelectivityProviders.PROVIDER_NON_DOMINATION, SelectivityProviders.PROVIDER_FRONT_COUNT, SelectivityProviders.PROVIDER_PREF_REL};
		
		System.out.printf("      ");
		for(int k=0; k<providers.length; k++) {
			System.out.printf("%29s | ", providers[k].toString());
		}
		System.out.println();
		
		System.out.printf("      ");
		for(int k=0; k<providers.length; k++) {
			System.out.printf("%29s | ", "selectivity stdev    duration");
		}
		System.out.println();
		
		for(int i=0; i<20; i++) {
			int paretoSize = 50 * (i + 1);
			AggregatedSelectivity[] selectivities = new AggregatedSelectivity[providers.length];
			System.out.printf("%4d: ", paretoSize);
			for(int k=0; k<providers.length; k++) {
				DTLZ2SelectivityTest test = new DTLZ2SelectivityTest(providers[k], objCount, paretoSize, maxRuns);
				AggregatedSelectivity s = selectivities[k] = test.getSelectivity(ruleCount, tupleSize, alphas);
				System.out.printf(Locale.US, "%8f    %8f %3d sec. | ", s.getSelectivity(), s.getStdev(), (s.getDuration() / 1000));
			}
			System.out.println();
		}
	}
}
