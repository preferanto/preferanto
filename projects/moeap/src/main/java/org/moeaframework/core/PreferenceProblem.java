package org.moeaframework.core;

import org.moeaframework.core.comparator.DominanceComparatorProvider;

public class PreferenceProblem implements Problem {
	private final Problem innerProblem;
	private final DominanceComparatorProvider preferenceProvider;
	
	public PreferenceProblem(Problem innerProblem, DominanceComparatorProvider preferenceProvider) {
		this.innerProblem = innerProblem;
		this.preferenceProvider = preferenceProvider;
	}

	@Override
	public String getName() {
		return innerProblem.getName();
	}

	@Override
	public int getNumberOfVariables() {
		return innerProblem.getNumberOfVariables();
	}

	@Override
	public int getNumberOfObjectives() {
		return innerProblem.getNumberOfObjectives();
	}

	@Override
	public int getNumberOfConstraints() {
		return innerProblem.getNumberOfConstraints();
	}

	@Override
	public void evaluate(Solution solution) {
		innerProblem.evaluate(solution);
	}

	@Override
	public Solution newSolution() {
		return innerProblem.newSolution();
	}

	@Override
	public void close() {
		innerProblem.close();
	}
	
	
	@Override
	public DominanceComparatorProvider getPreferenceComparatorProvider() {
		return preferenceProvider;
	}

}
