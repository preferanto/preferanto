package org.preferanto.experiment;

import java.io.BufferedReader;
import java.io.FileReader;

import org.apache.commons.math3.random.RandomAdaptor;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.DominanceComparatorProvider;
import org.moeaframework.core.spi.ProblemFactory;
import org.preferanto.experiment.moea.MOEAUtils;
import org.preferanto.experiment.moea.PreferantoComparatorProvider;
import org.preferanto.experiment.util.RandomUtil;
import org.preferanto.poset.PosetProvider;
import org.preferanto.poset.PosetUtil;

public class AlgoTest {
	private static String[] SERIES_NAMES = {"ref", "std", "pref"};

	String prefFileName = "prefs/obj2_1.pref";
//	String problemName = "DTLZ7_2";
	String problemName = "DTLZ2_2";
	String algorithmName = "NSGAII";
	int populationSize = 100;
	int maxEvaluations = 10000;
	int instrumenterFrequency = 100;
	
	long seed = 1;

	
	public static void main(String[] args) throws Exception {
		new AlgoTest().run();
	}
	
	public void run() throws Exception {
		RandomUtil.INSTANCE.setSeed(seed);
		PRNG.setRandom(new RandomAdaptor(RandomUtil.INSTANCE.getRandom()));
		
		try {

			BufferedReader reader = new BufferedReader(new FileReader(prefFileName));
			PosetProvider posetProvider = PosetUtil.getPosetProvider(reader);

			// *** LISTENERS *********************************************************************
//			posetProvider.addListener(new DurationListener());
//			ImageConfiguration imgConfig = new ImageConfiguration("jung", "algo", ".png");
//			posetProvider.addListener(new ImageListener(imgConfig));
			// ***********************************************************************************

			
			DominanceComparatorProvider standardPreferantoProvider = new PreferantoComparatorProvider(posetProvider);
			
//			DominanceComparatorProvider preferantoProvider = new PopulationIndependentDominanceComparatorProvider(new org.moeaframework.core.comparator.DominanceComparator() {				
//				@Override
//				public int compare(Solution solution1, Solution solution2) {
//					return 0;
//				}				
//			});

			DominanceComparatorProvider[] preferantoProviders = {null, standardPreferantoProvider};

			Accumulator[] accumulators = new Accumulator[preferantoProviders.length];

			for(int k=0; k<preferantoProviders.length; k++) {
				DominanceComparatorProvider preferantoProvider = preferantoProviders[k];

				long startTime = System.nanoTime();

				Instrumenter instrumenter = new Instrumenter()
					.withProblem(problemName)
					.withFrequency(instrumenterFrequency)
					.attachAll();
				
				NondominatedPopulation result = new Executor()
					.withProblem(problemName)
					.withAlgorithm(algorithmName)
					.withMaxEvaluations(maxEvaluations)
					.withProperty("populationSize", populationSize)
					.withPreferenceComparatorProvider(preferantoProvider)
					.withInstrumenter(instrumenter)
					.run();

				long endTime = System.nanoTime();
				
				System.out.println("\n\nresult:");
				for(Solution solution : result) {
					System.out.println(MOEAUtils.toString(solution));
				}
				
				System.out.println("\nResult computed in " + ((endTime - startTime) / 1000000.0) + " ms.");
				
//				MOEAUtils.toGnuplot(result, "dat/" + problemName + "-result-" + k + ".dat");
				
				accumulators[k] = instrumenter.getLastAccumulator();				
			}
			
			ApproximationSetFrame frame = new ApproximationSetFrame(
					problemName + " " + populationSize + " / " + maxEvaluations, 
					accumulators, 
					ProblemFactory.getInstance().getReferenceSet(problemName), 
					SERIES_NAMES);
			frame.run();
			
//			Analyzer analyzer = new Analyzer()
//				.withProblem("UF1")
//				.includeAllMetrics()
//				.showStatisticalSignificance();
//			
//			Executor executor = new Executor()
//				.withProblem("UF1")
//				.withMaxEvaluations(10000);
//			
//			analyzer.addAll("NSGAII", executor.withAlgorithm("NSGAII").runSeeds(50));
//			analyzer.addAll("GDE3", executor.withAlgorithm("GDE3").runSeeds(50));
//			
//	//		analyzer.showStatisticalSignificance();
//			analyzer.showAll();
//			analyzer.printAnalysis();

		
		} catch(Throwable e) {
			e.printStackTrace();
		}		
	}
	
}
