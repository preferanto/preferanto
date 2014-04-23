package org.preferanto.experiment.impact;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

public class SelectivityImpactInfo {
	private final int configCount;
	private final int runCount;
	
	// indexed by refNfe
	private final SortedMap<Integer, ConfigImpactInfo[]> configInfoMap = new TreeMap<Integer, ConfigImpactInfo[]>();
	
	public SelectivityImpactInfo(int configCount, int runCount) {
		this.configCount = configCount;
		this.runCount = runCount;
	}
	
	public SortedMap<Integer, ConfigImpactInfo[]> getConfigInfoMap() {
		return configInfoMap;
	}
	
	public ConfigImpactInfo getConfigImpactInfo(int configIndex, int refNfe) {
		ConfigImpactInfo[] infos = configInfoMap.get(refNfe);
		if(infos == null) {
			infos = new ConfigImpactInfo[configCount];
			configInfoMap.put(refNfe, infos);
		}
		ConfigImpactInfo info = infos[configIndex];
		if(info == null) {
			info = infos[configIndex] = new ConfigImpactInfo(runCount);
		}
		return info;
	}
	
	public void setNfe(int configIndex, int refNfe, int run, int nfe) {
		ConfigImpactInfo info = getConfigImpactInfo(configIndex, refNfe);
		info.getNfes()[run] = nfe;
	}
	
	/** Triggers the computation of {@link #meanNfe} and {@link #stdev} */
	public void complete() {
		for(ConfigImpactInfo[] infos : configInfoMap.values()) {
			for(ConfigImpactInfo info : infos) {
				info.complete();
			}
		}
	}
	
	public String toString(int configIndex) {
		StringBuilder sb = new StringBuilder();
		for(Entry<Integer, ConfigImpactInfo[]> entry : configInfoMap.entrySet()) {
			int refNfe = entry.getKey();
			ConfigImpactInfo[] infos = entry.getValue();
			sb.append(String.format("%6d, ", refNfe)).append(infos[configIndex]).append('\n');
		}		
		return sb.toString();
	}

	@Override
	public String toString() {
		return configInfoMap.toString();
	}
}
