package org.preferanto.experiment.util;


public class ObjectiveValuesGenerator {
	private final double minVal;
	private final double maxVal;

	public ObjectiveValuesGenerator(double minVal, double maxVal) {
		this.minVal = minVal;
		this.maxVal = maxVal;
	}
	
	public double[][] getValues(int populationSize, int objectiveCount) {
		double[][] values = new double[populationSize][objectiveCount];
		
		for(int i=0; i<populationSize; i++) {
			for(int j=0; j<objectiveCount; j++) {
				values[i][j] = RandomUtil.INSTANCE.nextDouble(minVal, maxVal);
			}
		}
		return values;
	}
}
