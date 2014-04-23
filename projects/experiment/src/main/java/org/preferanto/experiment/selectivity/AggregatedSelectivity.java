package org.preferanto.experiment.selectivity;

import java.util.Locale;

public class AggregatedSelectivity {
	public final double selectivity;
	public final double stdev;
	public final long duration;
	
	public AggregatedSelectivity(double selectivity, double stdev, long duration) {
		this.selectivity = selectivity;
		this.stdev = stdev;
		this.duration = duration;
	}
	
	public double getSelectivity() {
		return selectivity;
	}
	
	public double getStdev() {
		return stdev;
	}
	
	public long getDuration() {
		return duration;
	}
	
	@Override
	public String toString() {
		return toString(true);
	}

	public String toString(boolean showDuration) {
		String s = String.format(Locale.US, "selectivity: %8f, stdev: %8f", selectivity, stdev);
		if(showDuration) {
			s += String.format(Locale.US, ", duration:%3d sec.", (duration / 1000));
		}
		return s;
	}
}
