package org.preferanto.poset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.preferanto.core.EvaluatorCreatorBytecode;
import org.preferanto.core.PreferantoCompiler;
import org.preferanto.core.PreferantoContext;
import org.preferanto.core.PreferantoEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PosetProvider {
	private static final Logger log = LoggerFactory.getLogger(PosetProvider.class);

	private static final double MIN_DIFF = 1.0E-9;
	
	private final PreferantoEvaluator evaluator;
	
	private final List<Listener> listeners = new ArrayList<>();
	
	public static interface Listener {
		void getPosetStarted(Poset poset);
		void ruleStarted(int rule, Poset poset);
		void ruleFinished(int rule, Poset poset, List<Relation> relations, int relationsAdded);
		void getPosetFinished(Poset poset);
	}
	
	public PosetProvider(PreferantoEvaluator evaluator) {
		this.evaluator = evaluator;
	}
	
	public PosetProvider(String preferantoText) throws Exception {
		this(EvaluatorCreatorBytecode.createFrom(new PreferantoCompiler(preferantoText)));
	}

	public int getRuleCount() {
		return evaluator.getRuleCount();
	}

	public PreferantoEvaluator getEvaluator() {
		return evaluator;
	}
	
	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	public boolean isPopulationDependent() {
		return (evaluator.getConditionalRuleCount() != 0);
	}
	
	public Poset getPoset(List<PreferantoContext> contexts) {
		int size = contexts.size();
		Poset poset = new Poset(size);
		
		for(Listener listener : listeners) {
			listener.getPosetStarted(poset);
		}

		if(size > 1) {
			int ruleCount = evaluator.getRuleCount();
			if(!isPopulationDependent()) {
				for(int rule=1; rule<=ruleCount; rule++) {
					List<Relation> relations = getRelations(contexts, poset, rule);
					for(Relation rel : relations) {
						poset.set(rel.getIdxFrom(), rel.getIdxTo(), rule);
					}
				}
			} else {
				for(int rule=1; rule<=ruleCount; rule++) {				
					for(Listener listener : listeners) {
						listener.ruleStarted(rule, poset);
					}

					List<Relation> relations = getRelations(contexts, poset, rule);

					// TODO: skip sort if rule == 1 
					Collections.sort(relations);
					
					int relationsAdded = addRelations(relations, poset, rule);
					
					for(Listener listener : listeners) {
						listener.ruleFinished(rule, poset, relations, relationsAdded);
					}				
				}
			}
		}		
		
		for(Listener listener : listeners) {
			listener.getPosetFinished(poset);
		}

		return poset;
	}

	private List<Relation> getRelations(List<PreferantoContext> contexts, Poset poset, int rule) {
		int size = contexts.size();
		List<Relation> relations = new ArrayList<>(size * (size - 1) / 2);
		for(int i=0; i<size-1; i++) {
			for(int j=i+1; j<size; j++) {
				if(poset.getRule(i, j) == 0) {
					double[] diffs = evaluator.compare(rule-1, contexts.get(i), contexts.get(j));
					if(diffs != null) {
						double minDiff = diffs[0];
						double maxDiff = minDiff;
						double[] absDiffs = new double[diffs.length];
						for(int k=0; k<diffs.length; k++) {
							double val = diffs[k];
							if(val < minDiff) {
								minDiff = val;
							}
							if(val > maxDiff) {
								maxDiff = val;
							}
							absDiffs[k] = Math.abs(val);
						}
						if((minDiff < 0) && (maxDiff <= 0) && (Math.abs(minDiff) > MIN_DIFF)) {
							relations.add(new Relation(i, j, absDiffs));									
						} else if((maxDiff > 0) && (minDiff >= 0) && (maxDiff > MIN_DIFF)) {
							relations.add(new Relation(j, i, absDiffs));									
						}
					}
				}
			}
		}
		return relations;
	}
	
	private int addRelations(List<Relation> relations, Poset origPoset, int rule) {
		Poset poset = new Poset(origPoset);
		double[] lastDiffs = null;
		int added = 0;
		for(Relation rel : relations) {
			if(log.isTraceEnabled()) {
				log.trace("Adding relation " + rel + " to:\n" + poset);
			}
			double[] diffs = rel.getDiffs();
			if((lastDiffs != null) && !Arrays.equals(diffs, lastDiffs)) {
				origPoset.setRuleMatrix(poset.getRuleMatrix());
			}
			lastDiffs = diffs;
			
			int idxFrom = rel.getIdxFrom();
			int idxTo = rel.getIdxTo();

			if(poset.set(idxFrom, idxTo, rule)) added++;
			else break;
			
			if(log.isTraceEnabled()) {
				log.trace("poset after adding relation " + rel + ":\n" + poset);
			}
		}
		if(added == relations.size()) {
			origPoset.setRuleMatrix(poset.getRuleMatrix());
		}
		return added;
	}
}
