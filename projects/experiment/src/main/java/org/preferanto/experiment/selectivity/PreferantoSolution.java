package org.preferanto.experiment.selectivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jgap.IChromosome;

public class PreferantoSolution {	
	private final double[] alphas;
	private final AggregatedSelectivity aggregatedSelectivity;
	private final List<Double> bestSoFarHistory = new ArrayList<>();
	
	private PrefGen prefGen;
	
	public PreferantoSolution(int termCount, IChromosome chromosome) {
		alphas = new double[termCount];
		alphas[0] = 1;
		for(int i=1; i<termCount; i++) {
			alphas[i] = (Double)chromosome.getGene(i-1).getAllele();
		}
		Object appData = chromosome.getApplicationData();
		this.aggregatedSelectivity = (appData instanceof AggregatedSelectivity) ? (AggregatedSelectivity)appData : null;
	}

	public double[] getAlphas() {
		return alphas;
	}
	
	public AggregatedSelectivity getAggregatedSelectivity() {
		return aggregatedSelectivity;
	}
	
	public List<Double> getBestSoFarHistory() {
		return bestSoFarHistory;
	}
	
	public PrefGen getPrefGen() {
		return prefGen;
	}
	
	public void setPrefGen(PrefGen prefGen) {
		this.prefGen = prefGen;
	}
	
	@Override
	public String toString() {
		return toString(false, false);
	}
	
	public String toString(boolean showDuration, boolean showHistory) {
		String s = aggregatedSelectivity.toString(showDuration);
		s += " - {";
		if(prefGen != null) {
			s += prefGen.getObjCount() + ", " + prefGen.getRuleCount() + ", " + prefGen.getTupleSize() + ", ";
		}
		s += Arrays.toString(alphas) + "}";
		if(showHistory) {
			s += ", history: " + bestSoFarHistory;
		}
		return s;
	}
	
	public String toDumpString() {
		String preferantoText = (prefGen != null) ? prefGen.getPreferanto() : null;
		return "(" + Arrays.toString(alphas) + "): " + aggregatedSelectivity 
				+ ((preferantoText != null) ? ("\n" + preferantoText.substring(preferantoText.indexOf("preferences"))) : "");
	}
}
