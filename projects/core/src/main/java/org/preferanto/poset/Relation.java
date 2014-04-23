package org.preferanto.poset;

import java.util.Arrays;

import org.preferanto.core.Utils;

public class Relation implements Comparable<Relation> {
	private final int idxFrom;
	private final int idxTo;
	private final double[] diffs;

	public Relation(int idxFrom, int idxTo, double[] diffs) {
		this.idxFrom = idxFrom;
		this.idxTo = idxTo;
		this.diffs = diffs;
	}
	
	public int getIdxFrom() {
		return idxFrom;
	}
	
	public int getIdxTo() {
		return idxTo;
	}
	
	public double[] getDiffs() {
		return diffs;
	}

	@Override
	public int compareTo(Relation other) {
		int result = 0;
		for(int k=0; k<diffs.length; k++) {
			result = Utils.doubleCompare(diffs[k], other.diffs[k]);
			if(result != 0) break;
		}
		return result;
	}
	
	@Override
	public String toString() {
		return idxFrom + " -> " + idxTo + "(" + Arrays.toString(diffs) + ")";
	}
}