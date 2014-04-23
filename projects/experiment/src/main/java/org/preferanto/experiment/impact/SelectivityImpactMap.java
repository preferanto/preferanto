package org.preferanto.experiment.impact;

import java.util.SortedMap;
import java.util.TreeMap;

public class SelectivityImpactMap {
	private final int configCount;
	private final int runCount;
	private final SortedMap<Double, SelectivityImpactInfo> map = new TreeMap<Double, SelectivityImpactInfo>();
	
	public SelectivityImpactMap(int configCount, int runCount) {
		this.configCount = configCount;
		this.runCount = runCount;
	}
	
	public SortedMap<Double, SelectivityImpactInfo> getMap() {
		return map;
	}
	
	public SelectivityImpactInfo getImpactInfo(double selectivity) {
		SelectivityImpactInfo info = map.get(selectivity);
		if(info == null) {
			info = new SelectivityImpactInfo(configCount, runCount);
			map.put(selectivity, info);
		}
		return info;
	}

	public void setNfe(double selectivity, int configIndex, int refNfe, int run, int nfe) {
		getImpactInfo(selectivity).setNfe(configIndex, refNfe, run, nfe);
	}

	/** Triggers the computation of meanNfe and stdev for all {@link SelectivityImpactInfo}s */
	public void complete() {
		for(SelectivityImpactInfo info : map.values()) {
			info.complete();
		}
	}
}
