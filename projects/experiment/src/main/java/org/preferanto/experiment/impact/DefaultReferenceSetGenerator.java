package org.preferanto.experiment.impact;

import java.util.List;
import java.util.Locale;

import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.core.Solution;
import org.preferanto.experiment.util.MathUtil;

public class DefaultReferenceSetGenerator extends ReferenceSetGenerator {
	private String problemName;
	private String algorithmName = "NSGAII";
	private int populationSize = 100;
	private int maxEvaluations = 10000;
	private int instrumenterFrequency = 100;

	public DefaultReferenceSetGenerator(String inputDir, String baseOutputDir, int objCount) {
		super(inputDir, baseOutputDir, objCount);
		problemName = "DTLZ2_" + objCount;
	}

	@Override
	protected Executor createExecutor() {
		Instrumenter instrumenter = new Instrumenter()
		.withProblem(problemName)
		.withFrequency(instrumenterFrequency)
		.attachApproximationSetCollector();
	
		Executor executor = new Executor()
		.withProblem(problemName)
		.withAlgorithm(algorithmName)
		.withMaxEvaluations(maxEvaluations)
		.withProperty("populationSize", populationSize)
		.withInstrumenter(instrumenter);

		return executor;
	}

	@Override
	protected String getComment(int nfe, List<Solution> result) {
		String comment = "nfe: " + nfe;
		if(problemName.startsWith("DTLZ2_")) {
			int size = result.size();
			if(size > 0) {
				double[] diffs = new double[result.size()];
				for(int i=0; i<size; i++) {
					Solution solution = result.get(i);
					double sum = 0;
					for(double val : solution.getObjectives()) {
						sum += val * val;
					}
					diffs[i] = Math.sqrt(sum) - 1;
				}
				double stdev = MathUtil.stdev(diffs, false);
				double meanDiff = MathUtil.mean(diffs);
				comment += String.format(Locale.US, ", error: %.5f, stdev: %.5f", meanDiff, stdev);
			}
		}
		return comment;
	}
	
	public DefaultReferenceSetGenerator withProblem(String problemName) {
		this.problemName = problemName;
		return this;
	}

	public DefaultReferenceSetGenerator withAlgorithm(String algorithmName) {
		this.algorithmName = algorithmName;
		return this;
	}

	public DefaultReferenceSetGenerator withFrequency(int instrumenterFrequency) {
		this.instrumenterFrequency = instrumenterFrequency;
		return this;
	}

	public DefaultReferenceSetGenerator withMaxEvaluations(int maxEvaluations) {
		this.maxEvaluations = maxEvaluations;
		return this;
	}

	public DefaultReferenceSetGenerator withPopulationSize(int populationSize) {
		this.populationSize = populationSize;
		return this;
	}

	
	public static void main(String[] args) throws Exception {
		if(args.length < 2) {
			throw new IllegalArgumentException("Expected at least 2 arguments: provider minObjCount [maxObjCount [step]]");
		}
		String selProviderName = args[0];
		int minObjCount = Integer.parseInt(args[1]);
		if(minObjCount < 2) throw new IllegalArgumentException("Invalid value for minObjCount: " + minObjCount + ". Minimum value allowed: 2");
		int maxObjCount = (args.length > 2) ? Integer.parseInt(args[2]) : minObjCount;
		if(maxObjCount < minObjCount) throw new IllegalArgumentException("maxObjCount (" + maxObjCount + ") < minObjCount (" + minObjCount + ")");
		int objCountStep = (args.length > 3) ? Integer.parseInt(args[3]) : 1;
		
		
		String inputDir = "src/main/resources/selectivity/" + selProviderName;
		String baseOutputDir = "reference/" + selProviderName;
		
		int frequency = 100;
		int maxEvaluations = 10000;
		
		for(int objCount = minObjCount; objCount <= maxObjCount; objCount += objCountStep) {			
			System.out.println("Starting generator for objCount = " + objCount + "...");
			ReferenceSetGenerator generator = new DefaultReferenceSetGenerator(inputDir, baseOutputDir, objCount)
				.withFrequency(frequency)
				.withMaxEvaluations(maxEvaluations);
			try {
				generator.generate();
			} catch(Exception e) {
				e.printStackTrace();
			}
			System.out.println("Generator for objCount = " + objCount + " terminated.");
		}
	}
}
