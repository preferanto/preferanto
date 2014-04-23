package org.preferanto.experiment.impact;

import java.util.Arrays;

public class ImpactResult {
	private final int[] generationIndexes;
	private final int[] refNfes;
	private final int[] nfes;

	public ImpactResult(int[] generationIndexes, int[] refNfes, int[] nfes) {
		this.generationIndexes = generationIndexes;
		this.refNfes = refNfes;
		this.nfes = nfes;
	}

	public int[] getGenerationIndexes() {
		return generationIndexes;
	}
	
	public int[] getRefNfes() {
		return refNfes;
	}
	
	public int[] getNfes() {
		return nfes;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(generationIndexes);
	}
}
