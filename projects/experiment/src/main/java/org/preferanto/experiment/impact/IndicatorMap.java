package org.preferanto.experiment.impact;

import java.util.SortedMap;
import java.util.TreeMap;

public class IndicatorMap {
	private static final int SELECTIVITY_COUNT = 10;
	
	private final SortedMap<Integer, double[]> map = new TreeMap<>();
	
	public SortedMap<Integer, double[]> getMap() {
		return map;
	}
	
	public double[] getValues(int nfe) {
		double[] values = map.get(nfe);
		if(values == null) {
			values = new double[SELECTIVITY_COUNT];
			map.put(nfe, values);
		}
		return values;
	}
	
	public void setValue(int nfe, int selectivityIndex, double val) {
		getValues(nfe)[selectivityIndex] = val;
	}
}
