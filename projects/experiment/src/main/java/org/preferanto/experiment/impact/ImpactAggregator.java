package org.preferanto.experiment.impact;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

public class ImpactAggregator {
	private final String baseDir;

	public ImpactAggregator(String baseDir) {
		this.baseDir = baseDir;
	}
	
	public void aggregate(int objCount) throws IOException {
		List<SortedMap<Integer, Double>> maps = new ArrayList<>();
		for(int i=0; i<10; i++) {
			double selectivity = i * 0.1;
			String meanFile = String.format(Locale.US, "%s/objCount.%d/selectivity.%.1f/mean.txt", baseDir, objCount, selectivity);
			maps.add(getMeans(meanFile));
		}
		SortedMap<Integer, Double> map0 = maps.get(0);
		String outputFile  = String.format(Locale.US, "%s/objCount.%d/impact.txt", baseDir, objCount);
		try(PrintWriter writer = new PrintWriter(outputFile)) {
			for(int refNfe : map0.keySet()) {
				writer.printf(Locale.US, "%6d", refNfe);
				for(int i=0; i<10; i++) {
					double mean = maps.get(i).get(refNfe);
					writer.printf(Locale.US, ", %8.1f", mean);
				}
				writer.println();
			}
			
			boolean hasError = writer.checkError();
			if(hasError) {
				throw new IOException("Failed to write to " + outputFile);
			}
		}
		
		
	}
	
	public SortedMap<Integer, Double> getMeans(String meanFile) throws IOException {
		SortedMap<Integer, Double> map = new TreeMap<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(meanFile))) {
			while(true) {
				String line = reader.readLine();
				if(line == null) break;
				line = line.trim();
				if(line.isEmpty()) continue;
				if(line.startsWith("#")) continue;
				String[] items = line.split(",");
				if(items.length < 2) {
					throw new IOException("Invalid line in " + meanFile + ": " + line);
				}
				int refNfe = Integer.parseInt(items[0].trim());
				double mean = Double.parseDouble(items[1].trim());
				map.put(refNfe, mean);
			}
		}
		return map;
	}
	
	public static void main(String[] args) throws IOException {
		if(args.length < 2) {
			throw new IllegalArgumentException("Expected at least 2 arguments: provider minObjCount [maxObjCount [step]]");
		}
		String selProviderName = args[0];
		int minObjCount = Integer.parseInt(args[1]);
		if(minObjCount < 2) throw new IllegalArgumentException("Invalid value for minObjCount: " + minObjCount + ". Minimum value allowed: 2");
		int maxObjCount = (args.length > 2) ? Integer.parseInt(args[2]) : minObjCount;
		if(maxObjCount < minObjCount) throw new IllegalArgumentException("maxObjCount (" + maxObjCount + ") < minObjCount (" + minObjCount + ")");
		int objCountStep = (args.length > 3) ? Integer.parseInt(args[3]) : 1;

		
		String baseDir = "results/impact/" + selProviderName;
		ImpactAggregator aggregator = new ImpactAggregator(baseDir);

		for(int objCount = minObjCount; objCount <= maxObjCount; objCount += objCountStep) {			
			System.out.println("Aggregating impact results for objCount = " + objCount + "...");
			aggregator.aggregate(objCount);
		}		
	}
}
