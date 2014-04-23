package org.preferanto.experiment.selectivity;

import java.util.Locale;

public class SelectivityEntry {
	private final double selectivity;
	private final double stdev;
	private final PrefGen prefGen;
	

	public SelectivityEntry(double selectivity, double stdev, PrefGen prefGen) {
		this.selectivity = selectivity;
		this.stdev = stdev;
		this.prefGen = prefGen;
	}
	
	/**
	 * 
	 * @param line - a line containing data in the following format:
	 * <br/>objCount, selectivity, stdev, ruleCount, tupleSize, alphas... 
	 * <br/>Example:
	 * <br/>10, 0.100000, 0.007746, 1, 3, 0.0238249457, 0.1083851072, 0.7982074641
	 * <br/>The first value in the alphas array is always 1.0 and it does not appear in the line.
	 */
	public SelectivityEntry(String line) {
		String[] items = line.split(",");
		if(items.length < 6) throw new IllegalArgumentException("Too few arguments in line: " + line);
		int objCount = Integer.parseInt(items[0].trim());
		this.selectivity = Double.parseDouble(items[1].trim());
		this.stdev = Double.parseDouble(items[2].trim());
		int ruleCount = Integer.parseInt(items[3].trim());
		int tupleSize = Integer.parseInt(items[4].trim());
		double[] alphas = new double[items.length - 4];
		alphas[0] = 1.0;
		for(int i=1; i<alphas.length; i++) {
			alphas[i] = Double.parseDouble(items[i + 4].trim());
		}		
		this.prefGen = new PrefGen(objCount, ruleCount, tupleSize, alphas);
	}
	
	public double getSelectivity() {
		return selectivity;
	}
	
	public double getStdev() {
		return stdev;
	}
	
	public PrefGen getPrefGen() {
		return prefGen;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(Locale.US, "%2d, %3.1f, %8f,%2d,%2d", prefGen.getObjCount(), selectivity, stdev, prefGen.getRuleCount(), prefGen.getTupleSize()));
		double[] alphas = prefGen.getAlphas();
		for(int t=1; t<alphas.length; t++) {
			sb.append(String.format(Locale.US, ", %.4f", alphas[t]));
		}		
		return sb.toString();
	}
}
