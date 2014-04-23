package org.preferanto.experiment.impact;

import static org.preferanto.experiment.util.Constants.CONFIG_COUNT;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.preferanto.core.PreferantoContext;
import org.preferanto.experiment.util.ContextListBuilder;
import org.preferanto.experiment.util.MathUtil;
import org.preferanto.poset.Poset;
import org.preferanto.poset.PosetProvider;

public class IndicatorCalculator {
	private final String objCountInputDir;
	
	private final ContextListBuilder ctxBuilder = new ContextListBuilder();

	public IndicatorCalculator(String baseInputDir) throws IOException {
		this.objCountInputDir = baseInputDir;
	}

	public void fillIndicatorMap(IndicatorMap indicatorMap, int selectivityIndex) throws Exception {
		double selectivity = 0.1 * selectivityIndex;
		String selectivitySuffix = String.format(Locale.US, "%.1f", selectivity);
		String selectivityDir = objCountInputDir + "/selectivity." + selectivitySuffix;

		SortedMap<Integer, double[]> nondomRateMap = new TreeMap<>();
		
		for(int config = 0; config < CONFIG_COUNT; config++) {
			
			String selConfigDir = selectivityDir + "/config." + config;
			ReferenceSetReader selReader = new ReferenceSetReader(selConfigDir);
			PosetProvider posetProvider = selReader.getPosetProvider();			
			
			String noSelConfigDir = objCountInputDir + "/selectivity.0.0/config." + config;
			ReferenceSetReader noSelReader = new ReferenceSetReader(noSelConfigDir);
			for(int nfe : noSelReader.getNfes()) {
				List<double[]> noSelSolutions = noSelReader.readObjectiveValues(nfe);
				List<PreferantoContext> noSelContexts = ctxBuilder.createContextList(noSelSolutions);
				int noSelCount = noSelContexts.size();
				
				List<double[]> selSolutions = selReader.readObjectiveValues(nfe);
				List<PreferantoContext> selContexts = ctxBuilder.createContextList(selSolutions);

				List<PreferantoContext> contexts = new ArrayList<>(noSelContexts);
				contexts.addAll(selContexts);
					
				Poset poset = posetProvider.getPoset(contexts);
				int[][] ruleMatrix = poset.getRuleMatrix();
				int totalSize = poset.getSize();
				
				int count = 0;
				for(int i=0; i<noSelCount; i++) {
					boolean ok = true;
					for(int j=noSelCount; j < totalSize; j++) {
						if(ruleMatrix[i][j] < 0) {
							ok = false;
							break;
						}
					}
					if(ok) {
						count++;
					}
				}
				double indVal = (double)count / (double)noSelCount;
				double[] indVals = nondomRateMap.get(nfe);
				if(indVals == null) {
					indVals = new double[CONFIG_COUNT];
					nondomRateMap.put(nfe, indVals);
				}
				indVals[config] = indVal;
				
			}
		}
		for(Entry<Integer, double[]> entry : nondomRateMap.entrySet()) {
			int nfe = entry.getKey();
			double[] indVals = entry.getValue();
			double indVal = MathUtil.mean(indVals);
			indicatorMap.setValue(nfe, selectivityIndex, indVal);
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

		String baseInputDir = "reference/" + selProviderName;
		String baseOutputDir = "results/indicator/" + selProviderName;

		for(int objCount = minObjCount; objCount <= maxObjCount; objCount += objCountStep) {			
			System.out.println("Computing indicators for objCount = " + objCount + "...");
			String inputDir = baseInputDir + "/objCount." + objCount;
			File dir = new File(baseOutputDir);
			dir.mkdirs();
			if(!dir.isDirectory()) {
				throw new IOException("Cannot create directory " + baseOutputDir);
			}
			IndicatorCalculator calculator = new IndicatorCalculator(inputDir);
			IndicatorMap indicatorMap = new IndicatorMap();
			for(int selIndex=1; selIndex<10; selIndex++) {
				System.out.println("\t...selectivityIndex: " + selIndex);
				calculator.fillIndicatorMap(indicatorMap, selIndex);
			}
			String outputFile = baseOutputDir + "/indicator.all.objCount." + objCount + ".txt";
			try(PrintWriter writer = new PrintWriter(outputFile)) {
				for(Entry<Integer, double[]> entry : indicatorMap.getMap().entrySet()) {
					int nfe = entry.getKey();
					double[] values = entry.getValue();
					double mean = MathUtil.mean(values);
					double stdev = MathUtil.stdev(values, false);
					writer.printf(Locale.US, "%6d,%12.8f,%12.8f", nfe, mean, stdev);
					for(int i=1; i<values.length; i++) {
						writer.printf(Locale.US, ",%12.8f", values[i]);
					}						
					writer.println();
				}
				
				boolean hasError = writer.checkError();
				if(hasError) {
					throw new IOException("Failed to write to " + outputFile);
				}
			}
		}		
	}
}
