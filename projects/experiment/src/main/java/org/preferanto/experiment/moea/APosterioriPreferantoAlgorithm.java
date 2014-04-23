package org.preferanto.experiment.moea;

import java.io.NotSerializableException;
import java.io.Serializable;

import org.moeaframework.core.Algorithm;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;

public class APosterioriPreferantoAlgorithm<A extends Algorithm> implements Algorithm {
	private final A delegate;
	
	private NondominatedPopulation originalResult;
	private NondominatedPopulation preferantoResult;
	
	public APosterioriPreferantoAlgorithm(A delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public Problem getProblem() {
		return delegate.getProblem();
	}

	@Override
	public NondominatedPopulation getResult() {
		return isTerminated() ? preferantoResult : delegate.getResult();
	}

	@Override
	public void step() {
		delegate.step();
	}

	@Override
	public void evaluate(Solution solution) {
		delegate.evaluate(solution);
	}

	@Override
	public int getNumberOfEvaluations() {
		return delegate.getNumberOfEvaluations();
	}

	@Override
	public boolean isTerminated() {
		return delegate.isTerminated();
	}

	@Override
	public void terminate() {
		delegate.terminate();		
		
		this.originalResult = delegate.getResult();

		// TODO - apply preferanto!!!
//		this.preferantoResult = ...
		
		throw new AssertionError("terminate() NOT IMPLEMENTED!");
	
	}

	@Override
	public Serializable getState() throws NotSerializableException {
		return delegate.getState();
	}

	@Override
	public void setState(Object state) throws NotSerializableException {
		delegate.setState(state);
	}

	public NondominatedPopulation getOriginalResult() {
		return originalResult;
	}
}
