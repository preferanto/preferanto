package org.preferanto.experiment.impact;

import static org.preferanto.experiment.util.Constants.CONFIG_COUNT;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.math3.random.RandomAdaptor;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.PRNG;
import org.preferanto.experiment.impact.MeanImpactMap.Info;
import org.preferanto.experiment.moea.MOEAUtils;
import org.preferanto.experiment.util.LogConfig;
import org.preferanto.experiment.util.MathUtil;
import org.preferanto.experiment.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImpactCalculator {
	private static final Logger log = LoggerFactory.getLogger(ImpactCalculator.class);

	private final String baseRefDir;

	private String baseProblemName = "DTLZ2_";
	private String algorithmName = "NSGAII";
	private int populationSize = 100;
	private int maxEvaluations = 100 * 10000;

	public ImpactCalculator(String baseRefDir) {
		this.baseRefDir = baseRefDir;
	}

	public void setProblemName(String baseProblemName) {
		this.baseProblemName = baseProblemName;
	}

	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}

	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	public void setMaxEvaluations(int maxEvaluations) {
		this.maxEvaluations = maxEvaluations;
	}

	public String getProblemName(int objCount) {
		return baseProblemName + objCount;
	}

	public ImpactResult getImpact(int objCount, double selectivity, int configIndex) throws Exception {
		String inputDir = String.format(Locale.US, "%s/objCount.%d/selectivity.%.1f/config.%d", baseRefDir, objCount, selectivity, configIndex);

		ReferenceSetReader referenceSetReader = new ReferenceSetReader(inputDir);
		ImpactCollector impactCollector = new ImpactCollector(referenceSetReader);

		String problemName = getProblemName(objCount);

		Instrumenter instrumenter = new Instrumenter()
		.withProblem(problemName)
		.withFrequency(1)
		.attach(impactCollector);

	NondominatedPopulation result = new Executor()
		.withProblem(problemName)
		.withAlgorithm(algorithmName)
		.withMaxEvaluations(maxEvaluations)
		.withProperty("populationSize", populationSize)
		.withInstrumenter(instrumenter)
		.run();

		Accumulator accumulator = instrumenter.getLastAccumulator();
		int size = accumulator.size(ImpactCollector.KEY);
		if(size != accumulator.size(MOEAUtils.NUMBER_OF_EVAL_KEY)) {
			throw new RuntimeException("Accumulator with different sizes for '" + MOEAUtils.APPROX_SET_KEY + "' and '" + MOEAUtils.NUMBER_OF_EVAL_KEY + "'");
		}

		int[] generationIndexes = new int[size];
		int[] nfes = new int[size];
		for(int i=0; i<size; i++) {
			nfes[i] = (Integer)accumulator.get(MOEAUtils.NUMBER_OF_EVAL_KEY, i);
			generationIndexes[i] = (Integer)accumulator.get(ImpactCollector.KEY, i);
		}
		return new ImpactResult(generationIndexes, impactCollector.getRefNfes(), nfes);
	}

	public static void main(String[] args) throws Exception {
		LogConfig.setLogbackResource("/logback.impact.xml");

		if(args.length < 2) {
			throw new IllegalArgumentException("Expected at least 2 arguments: provider minObjCount [maxObjCount [step]]");
		}
		String selProviderName = args[0];
		int minObjCount = Integer.parseInt(args[1]);
		if(minObjCount < 2) throw new IllegalArgumentException("Invalid value for minObjCount: " + minObjCount + ". Minimum value allowed: 2");
		int maxObjCount = (args.length > 2) ? Integer.parseInt(args[2]) : minObjCount;
		if(maxObjCount < minObjCount) throw new IllegalArgumentException("maxObjCount (" + maxObjCount + ") < minObjCount (" + minObjCount + ")");
		int objCountStep = (args.length > 3) ? Integer.parseInt(args[3]) : 1;

		String baseRefDir = "reference/" + selProviderName;
		String baseOutputDir = "results/impact/" + selProviderName;

		int configCount = CONFIG_COUNT;
		int runsPerConfiguration = 10;
		long rndSeed = 1;


		RandomUtil.INSTANCE.setSeed(rndSeed);
		PRNG.setRandom(new RandomAdaptor(RandomUtil.INSTANCE.getRandom()));

		ImpactCalculator impactCalculator = new ImpactCalculator(baseRefDir);


		for(int objCount = minObjCount; objCount <= maxObjCount; objCount += objCountStep) {
			log.info("\n\n#####################\nobjCount = " + objCount + "\n#####################\n");

			SelectivityImpactMap selImpactMap = new SelectivityImpactMap(configCount, runsPerConfiguration);

			for(int selIndex = 0; selIndex < 10; selIndex++) {
				double selectivity = 0.1 * selIndex;
				SelectivityImpactInfo selImpactInfo = selImpactMap.getImpactInfo(selectivity);
				MeanImpactMap meanImpactMap = new MeanImpactMap(configCount);


				String outputDir = String.format(Locale.US, "%s/objCount.%d/selectivity.%.1f", baseOutputDir, objCount, selectivity);
				File dir = new File(outputDir);
				dir.mkdirs();
				if(!dir.isDirectory()) {
					throw new IOException("Cannot create directory " + outputDir);
				}

				for(int configIndex = 0; configIndex < configCount; configIndex++) {
					for(int run=0; run<runsPerConfiguration; run++) {
						long startTime = System.currentTimeMillis();
						ImpactResult impactResult = impactCalculator.getImpact(objCount, selectivity, configIndex);
						long duration = System.currentTimeMillis() - startTime;

						int[] generationIndexes = impactResult.getGenerationIndexes();
						int[] refNfes = impactResult.getRefNfes();
						int[] nfes = impactResult.getNfes();

						int count = generationIndexes.length;
						int prevGenIndex = -1;
						for(int i=0; i<count; i++) {
							int genIndex = generationIndexes[i];
							if(genIndex > prevGenIndex) {
								for(int k=prevGenIndex+1; k <= genIndex; k++) {
									selImpactInfo.setNfe(configIndex, refNfes[k], run, nfes[i]);
								}
							}
							prevGenIndex = genIndex;
						}

						log.info(String.format(Locale.US, "%2d, %f, %d, %d completed in %3d sec.: %s",
								objCount, selectivity, configIndex, run, (duration / 1000), Arrays.toString(generationIndexes)));
					}
				}
				selImpactInfo.complete();

				for(int configIndex = 0; configIndex < configCount; configIndex++) {
					String impactFile = outputDir + "/config." + configIndex + ".txt";
					try(PrintWriter writer = new PrintWriter(impactFile)) {
						writer.printf(Locale.US, "# objCount: %d, selectivity: %f, config: %d\n", objCount, selectivity, configIndex);
						writer.println(selImpactInfo.toString(configIndex));
						boolean hasError = writer.checkError();
						if(hasError) {
							throw new IOException("Failed to write to " + impactFile);
						}
					}
				}

				for(int configIndex = 0; configIndex < configCount; configIndex++) {
					for(int refNfe : selImpactInfo.getConfigInfoMap().keySet()) {
						ConfigImpactInfo info = selImpactInfo.getConfigImpactInfo(configIndex, refNfe);
						meanImpactMap.setInfo(configIndex, refNfe, info.getMeanNfe(), info.getStdev());
					}
				}

				SortedMap<Integer, double[]> meanMap = new TreeMap<Integer, double[]>();
				for(int refNfe : selImpactInfo.getConfigInfoMap().keySet()) {
					Info info = meanImpactMap.getInfo(refNfe);
					double mean = MathUtil.mean(info.getMeanNfes());
					double stdev = MathUtil.stdev(info.getMeanNfes(), false);
					meanMap.put(refNfe, new double[] {mean, stdev});
				}

				String meanImpactFile = outputDir + "/mean.txt";
				try(PrintWriter writer = new PrintWriter(meanImpactFile)) {
					writer.printf(Locale.US, "# objCount: %d, selectivity: %f\n", objCount, selectivity);

					for(Entry<Integer, double[]> entry : meanMap.entrySet()) {
						int refNfe = entry.getKey();
						double[] means = entry.getValue();
						writer.printf(Locale.US, "%6d, %8.1f, %8.1f\n", refNfe, means[0], means[1]);
					}

					boolean hasError = writer.checkError();
					if(hasError) {
						throw new IOException("Failed to write to " + meanImpactFile);
					}
				}
			}
		}
	}
}
