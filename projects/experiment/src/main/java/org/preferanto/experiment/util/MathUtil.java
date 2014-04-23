package org.preferanto.experiment.util;

import java.util.List;

public class MathUtil {
	public static interface ValueProvider {
		int size();
		double get(int i);
	}

	public static ValueProvider getProvider(final List<? extends Number> values) {
		return new ValueProvider() {			
			@Override
			public int size() {
				return values.size();
			}
			
			@Override
			public double get(int i) {
				return values.get(i).doubleValue();
			}
		};
	}
	
	public static double mean(ValueProvider provider) {
		int len = provider.size();
		double mean = 0;
		if(len > 0) {
			for(int i=0; i<len; i++) {
				mean += provider.get(i);
			}
			mean /= len;
		}
		return mean;
	}
	
	public static double mean(double[] values) {
		int len = values.length;
		double mean = 0;
		if(len > 0) {
			for(int i=0; i<len; i++) {
				mean += values[i];
			}
			mean /= len;
		}
		return mean;
	}

	public static double mean(int[] values) {
		int len = values.length;
		double mean = 0;
		if(len > 0) {
			for(int i=0; i<len; i++) {
				mean += values[i];
			}
			mean /= len;
		}
		return mean;
	}

	public static double stdev(ValueProvider provider, boolean sample) {
		int len = provider.size();
		double dev = 0;
		if(len > 1) {
			double mean = 0;
			for(int i=0; i<len; i++) {
				mean += provider.get(i);
			}
			mean /= len;
			double sum = 0;
			for(int i=0; i<len; i++) {
				double diff = provider.get(i) - mean;
				sum += diff * diff;
			}
			sum /= sample ? (len - 1) : len;
			dev = Math.sqrt(sum);
		}
		return dev;
	}

	public static double stdev(double[] values, boolean sample) {
		int len = values.length;
		double dev = 0;
		if(len > 1) {
			double mean = 0;
			for(int i=0; i<len; i++) {
				mean += values[i];
			}
			mean /= len;
			double sum = 0;
			for(int i=0; i<len; i++) {
				double diff = values[i] - mean;
				sum += diff * diff;
			}
			sum /= sample ? (len - 1) : len;
			dev = Math.sqrt(sum);
		}
		return dev;
	}

	public static double stdev(int[] values, boolean sample) {
		int len = values.length;
		double dev = 0;
		if(len > 1) {
			double mean = 0;
			for(int i=0; i<len; i++) {
				mean += values[i];
			}
			mean /= len;
			double sum = 0;
			for(int i=0; i<len; i++) {
				double diff = values[i] - mean;
				sum += diff * diff;
			}
			sum /= sample ? (len - 1) : len;
			dev = Math.sqrt(sum);
		}
		return dev;
	}

}
