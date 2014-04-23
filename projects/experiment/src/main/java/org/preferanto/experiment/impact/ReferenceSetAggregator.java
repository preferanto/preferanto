package org.preferanto.experiment.impact;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.preferanto.experiment.util.MathUtil;

import static org.preferanto.experiment.util.Constants.CONFIG_COUNT;

public class ReferenceSetAggregator {

	private final String baseDir;

	public ReferenceSetAggregator(String baseDir) {
		this.baseDir = baseDir;
	}
	
	public void aggregate(String selectivitySuffix) throws Exception {
		String selectivityDir = baseDir + "/" + selectivitySuffix;
		SortedMap<Integer, double[]> nfeDistancesMap = new TreeMap<>();
		for(int config=0; config<CONFIG_COUNT; config++) {
			SortedMap<Integer, double[]> nfeBestSolutionMap = new TreeMap<>();
			String configDir = selectivityDir + "/config." + config;
			ReferenceSetReader reader = new ReferenceSetReader(configDir);
			for(int nfe : reader.getNfes()) {
				List<double[]> valueList = reader.readObjectiveValues(nfe);
				double bestDist = Double.MAX_VALUE;
				double[] bestSolution = null;
				for(double[] values : valueList) {
					double sum = 0;
					for(double val : values) {
						sum += val * val;
					}
					double dist = Math.sqrt(sum) - 1;
					if(dist < bestDist) {
						bestDist = dist;
						bestSolution = values;
					}
				}
				double[] distances = nfeDistancesMap.get(nfe);
				if(distances == null) {
					distances = new double[CONFIG_COUNT];
					nfeDistancesMap.put(nfe, distances);
				}
				distances[config] = bestDist;
				nfeBestSolutionMap.put(nfe, bestSolution);				
			}
			String bestSolFileName = selectivityDir + "/best.config." + config + ".txt";
			try(PrintWriter writer = new PrintWriter(bestSolFileName)) {
				writer.println("# " + reader.getSelectivityEntry());
				for(Entry<Integer, double[]> entry : nfeBestSolutionMap.entrySet()) {
					int nfe = entry.getKey();
					double distance = nfeDistancesMap.get(nfe)[config];
					writer.printf(Locale.US, "%6d, %f", nfe, distance);
					double[] bestSolution = entry.getValue();
					for(double val : bestSolution) {
						writer.printf(Locale.US, ", %f", val);
					}
					writer.println();
					boolean hasError = writer.checkError();
					if(hasError) {
						throw new IOException("Failed to write to " + bestSolFileName);
					}
				}
			}			
		}

		String distancesFileName = baseDir + "/distances.selectivity." + selectivitySuffix + ".txt";
		try(PrintWriter writer = new PrintWriter(distancesFileName)) {
			for(Entry<Integer, double[]> entry : nfeDistancesMap.entrySet()) {
				int nfe = entry.getKey();
				double[] distances = entry.getValue();
				double meanDist = MathUtil.mean(distances);
				double stdev = MathUtil.stdev(distances, false);
				writer.printf(Locale.US, "%6d, %.8f, %.8f", nfe, meanDist, stdev);
				for(int i=0; i<distances.length; i++) {
					writer.printf(Locale.US, ", %.10f", distances[i]);
				}
				writer.println();
				boolean hasError = writer.checkError();
				if(hasError) {
					throw new IOException("Failed to write to " + distancesFileName);
				}
			}
		}		
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
		
		String refDir = "reference";
		
		for(int objCount = minObjCount; objCount <= maxObjCount; objCount += objCountStep) {
			System.out.println("*** objCount: " + objCount + " ***");
			String objCountDir = refDir + "/" + selProviderName + "/objCount." + objCount;
			ReferenceSetAggregator aggregator = new ReferenceSetAggregator(objCountDir);
			for(int selIndex=0; selIndex<10; selIndex++) {
				double selectivity = 0.1 * selIndex;
				String selectivitySuffix = String.format(Locale.US, "selectivity.%.1f", selectivity);
				System.out.println("Aggregating " + selectivitySuffix + "...");
				aggregator.aggregate(selectivitySuffix);
			}
		}
	}
}
