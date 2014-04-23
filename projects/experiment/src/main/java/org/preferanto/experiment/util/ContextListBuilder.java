package org.preferanto.experiment.util;

import java.util.ArrayList;
import java.util.List;

import org.preferanto.core.PreferantoContext;
import org.preferanto.core.PreferantoContextImpl;

public class ContextListBuilder {
	private final String objNamePrefix;

	public ContextListBuilder() {
		this("z");
	}
	
	public ContextListBuilder(String objNamePrefix) {
		this.objNamePrefix = objNamePrefix;
	}
	
	public List<PreferantoContext> createContextList(List<double[]> values) {
		List<PreferantoContext> contexts = new ArrayList<>();
		for(double[] solution : values) {
			contexts.add(createContext(solution));
		}
		return contexts;
	}
	
	public List<PreferantoContext> createContextList(double[][] values) {
		return createContextList(values, null);
	}
	
	public List<PreferantoContext> createContextList(double[][] values, int[] permutation) {
		List<PreferantoContext> contexts = new ArrayList<>();
		int populationSize = values.length;
		for(int i=0; i<populationSize; i++) {
			int index = (permutation == null) ? i : permutation[i];
			PreferantoContext context = createContext(values[index]);
			contexts.add(context);
		}		
		return contexts;
	}
	
	public PreferantoContext createContext(double[] values) {
		int objectiveCount = values.length;
		PreferantoContextImpl context = new PreferantoContextImpl();
		for(int i=0; i<objectiveCount; i++) {
			context.setDouble(objNamePrefix + i, values[i]);
		}
		return context;
	}
}
