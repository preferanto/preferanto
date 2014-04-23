package org.preferanto.poset;

import java.io.Serializable;

public class Poset implements Serializable {
	private static final long serialVersionUID = 1L;

	private final int size;
	private final int[][] ruleMatrix;
	
	public Poset(int size) {
		this.size = size;
		this.ruleMatrix = new int[size][size];
	}

	public Poset(Poset poset) {
		this.size = poset.size;
		this.ruleMatrix = new int[size][];
		for(int i=0; i<size; i++) {
			ruleMatrix[i] = new int[size];
			System.arraycopy(poset.ruleMatrix[i], 0, ruleMatrix[i], 0, size);
		}
	}

	public void setRuleMatrix(int[][] matrix) {
		for(int i=0; i<size; i++) {
			System.arraycopy(matrix[i], 0, ruleMatrix[i], 0, size);
		}
	}
	
	public boolean[][] getConnected() {
		boolean[][] connected = new boolean[size][size];
		for(int i=0; i<size; i++) {
			for(int j=0; j<size; j++) {
				if(ruleMatrix[i][j] > 0) {
					connected[i][j] = true;
				}
			}
		}
		return connected;
	}
	
	public int getSize() {
		return size;
	}
	
	public int[][] getRuleMatrix() {
		return ruleMatrix;
	}

	public int getRule(int i, int j) {
		return ruleMatrix[i][j];
	}

	/** @return false if the added relation produces a cycle */
	public boolean set(int i, int j, int rule) {
//		boolean existing = false;
		if(ruleMatrix[i][j] != 0) {
			if(ruleMatrix[i][j] == rule) {
				return true;
//				existing = true;
			} else if(ruleMatrix[i][j] == -rule) {
				return false;
			} else {
				throw new IllegalArgumentException("set(" + i + ", " + j + ", " + rule + ") is illegal: relation already present in poset:\n" + this);
			}
		}
		ruleMatrix[i][j] = rule;
		ruleMatrix[j][i] = -rule;
		
		for(int h=0; h<size; h++) {
			if((h == i) || (ruleMatrix[h][i] > 0)) {
				for(int k=0; k<size; k++) {
					if((j == k) || ruleMatrix[j][k] > 0) {
						if(ruleMatrix[h][k] == 0) {
//							if(existing) {
//								throw new IllegalArgumentException("set(" + i + ", " + j + ", " + rule + ") is illegal: relation existing in poset:\n" + this);
//							}
							ruleMatrix[h][k] = rule;
							ruleMatrix[k][h] = -rule;
						} else if(ruleMatrix[h][k] < 0) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Warshall's algorithm
	 */
	public void transitiveClosure() {
		for(int k=0; k<size; k++) {
			for(int i=0; i<size; i++) {
				for(int j=0; j<size; j++) {
					if(i != j) {
						int minRule = Math.min(ruleMatrix[i][k], ruleMatrix[k][j]);
						int maxRule = Math.max(ruleMatrix[i][k], ruleMatrix[k][j]);
						if(minRule > 0) {
							if(ruleMatrix[i][j] == 0) {
								set(i, j, maxRule);
							} else if(ruleMatrix[i][j] < 0) {
								throw new AssertionError("Cycle detected: " + i + " <-> " + j + " in:\n" + this);
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int digits = 1 + (int)Math.round(Math.log10(size - 1));
		String format = "%" + digits + "d";
		for(int i=0; i < digits + 3; i++) sb.append(' ');
		for(int i=0; i < size; i++) sb.append(String.format(format, i)).append(' ');
		sb.append('\n');
		for(int i=0; i < digits + 1; i++) sb.append(' ');
		for(int i=0; i < size * (digits + 1) + 2; i++) sb.append('-');
		sb.append('\n');
		for(int i=0; i < size; i++) {
			sb.append(String.format(format, i)).append(" | ");
			for(int j=0; j < size; j++) sb.append(String.format(format, ruleMatrix[i][j])).append(' ');
			sb.append('\n');
		}
		return sb.toString();
	}
}
