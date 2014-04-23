package org.preferanto.experiment.selectivity;

import static org.preferanto.experiment.util.Constants.CONFIG_COUNT;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.DoubleGene;
import org.preferanto.core.PreferantoException;
import org.preferanto.experiment.util.LogConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreferantoSelectivitySolver {
	private static final Logger log = LoggerFactory.getLogger(PreferantoSelectivitySolver.class);
	private static final int MAX_EVOLUTIONS = 10;
	private static final double EPSILON = 0.001;
	private static final double[] ABORT_FITNESS = {0.9, 0.95, 0.97, 0.98, 0.99};
	private static final double MAX_DIFF_START = 0.25;
	private static final double MAX_DIFF_INCREMENT = 0.1;
	private static final double ABORT_MAX_DIFF = 0.6;

	private final SelectivityProvider selectivityProvider;
	private final int objCount; 
	private final int ruleCount;
	private final int tupleSize;
	private final int termCount;
	private final int paretoSize;
	private final int maxRuns;
	private final int populationSize;

	public PreferantoSelectivitySolver(SelectivityProvider selectivityProvider,  
			int objCount, int ruleCount, int tupleSize, int termCount, int paretoSize, int maxRuns, int populationSize) {
		this.selectivityProvider = selectivityProvider;
		this.objCount = objCount;
		this.ruleCount = ruleCount;
		this.tupleSize = tupleSize;
		this.termCount = termCount;
		this.paretoSize = paretoSize;
		this.maxRuns = maxRuns;
		this.populationSize = populationSize;
	}

	public PreferantoSolution getSolution(double desiredSelectivity) {
		try {
			Configuration conf = new DefaultConfiguration();
			SelectivityFitnessFunction fitnessFunction = new SelectivityFitnessFunction(
					selectivityProvider, desiredSelectivity, objCount, ruleCount, tupleSize, termCount, paretoSize, maxRuns);
			conf.setFitnessFunction(fitnessFunction);

			Gene[] sampleGenes = new Gene[termCount-1];
			for(int i=0; i<termCount-1; i++) {
				sampleGenes[i] = new DoubleGene(conf, 0.0001, 0.9999);				
			}

			Chromosome sampleChromosome = new Chromosome(conf, sampleGenes);

			conf.setSampleChromosome(sampleChromosome);

			conf.setPopulationSize(populationSize);

			Genotype population = Genotype.randomInitialGenotype( conf );
			
			PreferantoSolution bestSoFarSolution = null;
			double bestSoFarFitness = 0;
			List<Double> bestSoFarHistory = new ArrayList<>();
			for(int i=0; i<MAX_EVOLUTIONS; i++) {
				log.debug("Generation " + i);
				population.evolve();
				IChromosome chromosome = population.getFittestChromosome();
				double fitnessValue = chromosome.getFitnessValue();
				if(fitnessValue > bestSoFarFitness) {
					bestSoFarSolution = new PreferantoSolution(termCount, chromosome);
					bestSoFarFitness = fitnessValue;
					log.debug("New best so far: " + bestSoFarSolution);
					if(1 - bestSoFarFitness <= EPSILON) {
						break;
					}
				}
				bestSoFarHistory.add(bestSoFarFitness);
				
				double abortFitness = (i < ABORT_FITNESS.length) ? ABORT_FITNESS[i] : ABORT_FITNESS[ABORT_FITNESS.length - 1];
				if(bestSoFarFitness < abortFitness) break;
			}
			if(bestSoFarSolution != null) {
				PrefGen prefGen = new PrefGen(objCount, ruleCount, tupleSize, bestSoFarSolution.getAlphas());
				bestSoFarSolution.setPrefGen(prefGen);
				bestSoFarSolution.getBestSoFarHistory().addAll(bestSoFarHistory);
			}
			return bestSoFarSolution;
		} catch(InvalidConfigurationException e) {
			throw new PreferantoException(e);
		}
	}
	
	public static void main(String[] args) {
		LogConfig.setLogbackResource("/logback.selectivity.solver.xml");

		if(args.length < 2) {
			throw new IllegalArgumentException("Expected at least 2 arguments: provider minObjCount [maxObjCount [step]]");
		}
		String selProviderName = args[0];
		SelectivityProvider selectivityProvider = SelectivityProviders.getForName(selProviderName);
		int minObjCount = Integer.parseInt(args[1]);
		if(minObjCount < 2) throw new IllegalArgumentException("Invalid value for minObjCount: " + minObjCount + ". Minimum value allowed: 2");
		int maxObjCount = (args.length > 2) ? Integer.parseInt(args[2]) : minObjCount;
		if(maxObjCount < minObjCount) throw new IllegalArgumentException("maxObjCount (" + maxObjCount + ") < minObjCount (" + minObjCount + ")");
		int objCountStep = (args.length > 3) ? Integer.parseInt(args[3]) : 1;
		int startSelectivity = (args.length > 4) ? Integer.parseInt(args[4]) : 0;
		
		String outputDirName = "results/selectivity/" + selProviderName;
		File outputDir = new File(outputDirName);
		outputDir.mkdirs();
		if(!outputDir.isDirectory()) {
			log.error("Cannot create output directory " + outputDirName);
			return;
		}
		
		int repeatsPerConfig = 1;
		int paretoSize = 100;
		int maxRuns = 10;
		int populationSize = 100;
		int maxGoodSolutions = CONFIG_COUNT;

		for(int objCount = minObjCount; objCount <= maxObjCount; objCount += objCountStep) {			
			log.info("\n\n#####################\n" + selectivityProvider + "\nobjCount = " + objCount + "\n#####################\n");
			
			int maxTupleSize = objCount;
			int maxRuleCount = objCount - 1;
			int maxTermCount = 4;
			
			log.info("paretoSize: " + paretoSize + ", maxRuns: " + maxRuns + ", populationSize: " + populationSize + ", repeatsPerConfig: " + repeatsPerConfig);
			
			String outputName = outputDirName + "/pref.objCount." + objCount + ".txt" ;
			try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputName, true)))) {
				if(startSelectivity == 0) {
					for(int solCount = 0; solCount < maxGoodSolutions; solCount++) {
						PrefGen prefGen = new PrefGen(objCount, 1, objCount, new double[] {1.0, 0.0});
						SelectivityEntry entry = new SelectivityEntry(0.0, 0.0, prefGen);
						out.println(entry);
						out.flush();
					}
				}
				
				for(int i=Math.max(startSelectivity, 1); i<10; i++) {
					double desiredSelectivity = 0.1 * i;
					log.info(String.format(Locale.US, "\n\nselectivity: %2f\n-------------------------\n", desiredSelectivity));
					int goodSolutionsCount = 0;
					double[][] selectivities = new double[maxRuleCount+1][maxTupleSize+1];
					double maxDiff = MAX_DIFF_START;
					double bestDiff = Double.MAX_VALUE;
					while(goodSolutionsCount < maxGoodSolutions) {
						ruleCountLoop:
						for(int ruleCount=1; ruleCount <= maxRuleCount; ruleCount++) {
							if(ruleCount > 1) {
								double minSel = 0;
								for(int tupleSize=2; tupleSize <= maxTupleSize; tupleSize++) {
									double sel = selectivities[ruleCount - 1][tupleSize];
									if((minSel == 0) || ((sel != 0) && (sel < minSel))) {
										minSel = sel;
									}
								}
								if((minSel != 0) && (minSel - desiredSelectivity > maxDiff)) {
									break ruleCountLoop;
								}
							}
							tupleSizeLoop:
							for(int tupleSize=2; tupleSize <= maxTupleSize; tupleSize++) {
								if(tupleSize > 2) {
									double sel = selectivities[ruleCount][tupleSize - 1];
									if((sel != 0) && (desiredSelectivity - sel > maxDiff)) {
										break tupleSizeLoop;
									}
								}
								termCountLoop:
								for(int termCount=2; termCount <= Math.min(maxTermCount, objCount); termCount++) {
									for(int k=0; k<repeatsPerConfig; k++) {
										long startTime = System.currentTimeMillis();
										Configuration.reset();
										PreferantoSelectivitySolver solver = new PreferantoSelectivitySolver(
												selectivityProvider, objCount, ruleCount, tupleSize, termCount, paretoSize, maxRuns, populationSize);
										PreferantoSolution solution = solver.getSolution(desiredSelectivity);
										long duration = System.currentTimeMillis() - startTime;
										AggregatedSelectivity aggrSel = solution.getAggregatedSelectivity();
										double selectivity = aggrSel.getSelectivity();
										double diff = Math.abs(desiredSelectivity - selectivity);
										if(diff < bestDiff) {
											bestDiff = diff;
										}
										double oldSelectivity = selectivities[ruleCount][tupleSize]; 
										double oldDiff = Math.abs(desiredSelectivity - oldSelectivity);
										if((oldSelectivity == 0) || (diff < oldDiff)) {
											selectivities[ruleCount][tupleSize] = selectivity;
										}
										boolean goodSolution = diff <= EPSILON;
										log.info("\t" + (goodSolution ? "* " : "  ") + solution.toString(false, true) + " computed in " + (duration / 1000) + " sec.");
										if(goodSolution) {
											PrefGen prefGen = new PrefGen(objCount, ruleCount, tupleSize, solution.getAlphas());
											SelectivityEntry entry = new SelectivityEntry(desiredSelectivity, aggrSel.getStdev(), prefGen);
											out.println(entry);
											out.flush();

											goodSolutionsCount++;
											if(goodSolutionsCount >= maxGoodSolutions) {
												break ruleCountLoop;
											}
											
										}
										if(diff > maxDiff) break termCountLoop;
									}
								}						
							}
						}
						if(goodSolutionsCount == 0) {
							maxDiff += MAX_DIFF_INCREMENT;
							if((maxDiff > ABORT_MAX_DIFF) && (bestDiff > 5 * EPSILON)) break;
						}
					}
				}
			} catch(IOException e) {
				log.error("Failed to write to the output file", e);
			}			
		}
	}
}
