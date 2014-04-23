package org.preferanto.experiment.impact;

import java.util.SortedMap;
import java.util.TreeMap;

import org.preferanto.experiment.selectivity.SelectivityEntry;

public class BestSolutionsMap {

	private final SelectivityEntry selectivityEntry;
	private final SortedMap<Integer, SolutionData> solutionMap = new TreeMap<>();
	
	public static class SolutionData {
		private final double distance;
		private final double[] values;

		public SolutionData(double distance, double[] values) {
			this.distance = distance;
			this.values = values;
		}
		
		public double getDistance() {
			return distance;
		}

		public double[] getValues() {
			return values;
		}		
	}
	
	public BestSolutionsMap(SelectivityEntry selectivityEntry) {
		this.selectivityEntry = selectivityEntry;
	}

	public SelectivityEntry getSelectivityEntry() {
		return selectivityEntry;
	}
	
	public SortedMap<Integer, SolutionData> getSolutionMap() {
		return solutionMap;
	}
	
	public void putSolutionData(int nfe, double distance, double[] values) {
		solutionMap.put(nfe, new SolutionData(distance, values));
	}
}
