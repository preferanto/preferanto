package org.preferanto.experiment.impact;

import java.io.FileReader;
import java.io.Reader;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.DominanceComparatorProvider;
import org.preferanto.experiment.moea.MOEAUtils;
import org.preferanto.experiment.moea.PreferantoComparatorProvider;
import org.preferanto.experiment.selectivity.PrefGen;
import org.preferanto.experiment.selectivity.SelectivityEntry;
import org.preferanto.experiment.selectivity.SelectivityReader;
import org.preferanto.poset.PosetProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ReferenceSetGenerator {
	private static final Logger log = LoggerFactory.getLogger(ReferenceSetGenerator.class);

	private String inputDir;
	private String baseOutputDir;
	private final int objCount;

	/**
	 *  @return - an {@link Executor}, which also has an {@link Instrumenter}. 
	 *            The {@link Executor} is fully configured, except for the preferenceComparatorProvider. 
	 */
	protected abstract Executor createExecutor();
	
	protected abstract String getComment(int nfe, List<Solution> result);
	
	public ReferenceSetGenerator(String inputDir, String baseOutputDir, int objCount) {
		this.inputDir = inputDir;
		this.baseOutputDir = baseOutputDir;
		this.objCount = objCount;
	}

	public void generate() throws Exception {
		String objCountDir = baseOutputDir + "/objCount." + objCount;
		Reader reader = new FileReader(inputDir + "/pref.objCount." + objCount + ".txt");
		SortedMap<Double, List<SelectivityEntry>> selMap = SelectivityReader.INSTANCE.getMap(reader, objCount);
		for(Entry<Double, List<SelectivityEntry>> selMapEntry : selMap.entrySet()) {
			double selectivity = selMapEntry.getKey();
			String selectivityDir = objCountDir + "/selectivity." + selectivity;
			List<SelectivityEntry> entries = selMapEntry.getValue();
			for(int i=0; i<entries.size(); i++) {
				String outDir = selectivityDir + "/config." + i;
				SelectivityEntry entry = entries.get(i);
				ReferenceSetWriter writer = new ReferenceSetWriter(outDir, entry);
				long startTime = System.currentTimeMillis();
				writeReferenceSets(writer);
				long duration = System.currentTimeMillis() - startTime;
				log.info("objCount." + objCount + "/selectivity." + selectivity + "/config." + i + " generated in " + (duration / 1000) + " sec.");
			}
		}
	}
	
	private void writeReferenceSets(ReferenceSetWriter writer) throws Exception {
		PrefGen prefGen = writer.getPrefGen();
		PosetProvider posetProvider = new PosetProvider(prefGen.getPreferanto());
		DominanceComparatorProvider preferantoProvider = new PreferantoComparatorProvider(posetProvider);
		
		Executor executor = createExecutor();		
		executor.withPreferenceComparatorProvider(preferantoProvider);
		
		/*NondominatedPopulation finalResult = */executor.run();
		
		Instrumenter instrumenter = executor.getInstrumenter();
		if(instrumenter == null) {
			throw new IllegalArgumentException("No instrumenter configured");
		}
		Accumulator accumulator = instrumenter.getLastAccumulator();
		int size = accumulator.size(MOEAUtils.APPROX_SET_KEY);
		if(size != accumulator.size(MOEAUtils.NUMBER_OF_EVAL_KEY)) {
			throw new RuntimeException("Accumulator with different sizes for '" + MOEAUtils.APPROX_SET_KEY + "' and '" + MOEAUtils.NUMBER_OF_EVAL_KEY + "'");
		}
		for(int i=0; i<size; i++) {
			int nfe = (Integer)accumulator.get(MOEAUtils.NUMBER_OF_EVAL_KEY, i);
			@SuppressWarnings("unchecked")
			List<Solution> result = (List<Solution>)accumulator.get(MOEAUtils.APPROX_SET_KEY, i);
			String comment = getComment(nfe, result);
			writer.writeObjectives(nfe, result, comment);
		}
	}	
}
