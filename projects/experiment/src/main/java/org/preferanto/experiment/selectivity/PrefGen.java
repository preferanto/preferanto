package org.preferanto.experiment.selectivity;

import java.util.Locale;


public class PrefGen {
	private final int objCount;
	private final int ruleCount;
	private final int termCount; 
	private final int tupleSize;
	private final double[] alphas;
	
	public PrefGen(int objCount, int ruleCount, int tupleSize, double[] alphas) {
		this.objCount = objCount;
		this.ruleCount = ruleCount;
		this.tupleSize = tupleSize;
		this.termCount = alphas.length;
		this.alphas = alphas;
	}
	
	public PrefGen(int objCount, int ruleCount, int tupleSize, int termCount, double alpha) {
		this(objCount, ruleCount, tupleSize, makeAlpha(termCount, alpha));
	}
	
	private static double[] makeAlpha(int termCount, double alpha) {
		double[] alphas = new double[termCount];
		alphas[0] = 1;
		for(int i=1; i<termCount; i++) {
			alphas[i] = Math.pow(alpha, i);
		}
		return alphas;
	}
	
	public int getObjCount() {
		return objCount;
	}
	
	public int getRuleCount() {
		return ruleCount;
	}
	
	public int getTermCount() {
		return termCount;
	}
	
	public int getTupleSize() {
		return tupleSize;
	}
	
	public double[] getAlphas() {
		return alphas;
	}
	
	public String getPreferanto() {
		StringBuilder sb = new StringBuilder(getQuantities());
		sb.append("\npreferences {\n");
		for(int rule=0; rule<ruleCount; rule++) {
			sb.append("\t").append(getTuple(rule)).append(";\n");
		}
		sb.append("}\n");
		return sb.toString();
	}
	
	private String getQuantities() {
		StringBuilder sb = new StringBuilder();
		sb.append("quantities {\n");
		for(int i=0; i<objCount; i++) {
			sb.append("\tz").append(i).append(": real;\n");
		}
		sb.append("}\n");
		return sb.toString();		
	}

	private String getTuple(int rule) {
		StringBuilder sb = new StringBuilder();
		sb.append('<');
		String sep = "";
		for(int k=0; k<tupleSize; k++) {
			sb.append(sep).append(getTupleElement(rule + k));
			sep = ", ";
		}
		sb.append('>');
		return sb.toString();
	}
	
	private String getTupleElement(int k) {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for(int i=0; i<termCount; i++) {
			String term = getTupleTerm(k, i);
			sb.append(sep).append(term);
			sep = "+";
		}
		return sb.toString();
	}

	private String getTupleTerm(int k, int i) {
		int zIdx = (k + i) % objCount;
		return ((alphas[i] == 1) ? "" : (String.format(Locale.US, "%.4f", alphas[i]) + "*")) + "z" + zIdx;
	}

	@Override
	public String toString() {
		return getPreferanto();
	}
	
	public static void main(String[] args) {
		PrefGen prefGen = new PrefGen(7, 6, 5, 4, 0.1);
		String preferanto = prefGen.getPreferanto();
		System.out.println(preferanto);
	}
}
