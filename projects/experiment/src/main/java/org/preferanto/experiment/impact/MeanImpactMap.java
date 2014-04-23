package org.preferanto.experiment.impact;

import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;

public class MeanImpactMap {
	private final int configCount;

	// indexed by refNfe 
	private final SortedMap<Integer, Info> map = new TreeMap<Integer, Info>();
	
	public static class Info {
		private final double[] meanNfes;
		private final double[] stdevs;
		
		public Info(int configCount) {
			this.meanNfes = new double[configCount];
			this.stdevs = new double[configCount];
		}
		
		public double[] getMeanNfes() {
			return meanNfes;
		}
		
		public double[] getStdevs() {
			return stdevs;
		}
		
		@Override
		public String toString() {
			return Arrays.toString(meanNfes);
		}
	}
	
	public MeanImpactMap(int configCount) {
		this.configCount = configCount;
	}

	public SortedMap<Integer, Info> getMap() {
		return map;
	}
	
	public Info getInfo(int refNfe) {
		Info info = map.get(refNfe);
		if(info == null) {
			info = new Info(configCount);
			map.put(refNfe, info);
		}
		return info;
	}

	public void setInfo(int configIndex, int refNfe, double meanNfe, double stdev) {
		Info info = getInfo(refNfe);
		info.meanNfes[configIndex] = meanNfe;
		info.stdevs[configIndex] = stdev;
	}
	
	@Override
	public String toString() {
		return map.toString();
	}
}
