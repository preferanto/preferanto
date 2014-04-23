package org.preferanto.experiment.impact;

import java.util.Locale;

import org.preferanto.experiment.util.MathUtil;

public class ConfigImpactInfo {
	private final int[] nfes;
	private double meanNfe = -1;
	private double stdev = -1;
	
	public ConfigImpactInfo(int runCount) {
		this.nfes = new int[runCount];
	}
	
	public int[] getNfes() {
		return nfes;
	}
	
	public double getMeanNfe() {
		if(meanNfe < 0) {
			meanNfe = MathUtil.mean(nfes);
		}
		return meanNfe;
	}
	
	public double getStdev() {
		if(stdev < 0) {
			stdev = MathUtil.stdev(nfes, false);
		}
		return stdev;
	}
	
	/** Triggers the computation of {@link #meanNfe} and {@link #stdev} */
	public void complete() {
		getMeanNfe();
		getStdev();
	}
	
	@Override
	public String toString() {
//		return String.format(Locale.US, "%.4f, %.5f, %s", meanNfe, stdev, Arrays.toString(nfes));
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(Locale.US, "%8.1f, %8.1f", meanNfe, stdev));
		for(int nfe : nfes) {
			sb.append(String.format(Locale.US, ",%7d", nfe));
		}
		return sb.toString();
	}	
}
