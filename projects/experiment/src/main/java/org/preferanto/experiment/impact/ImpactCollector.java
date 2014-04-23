package org.preferanto.experiment.impact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.analysis.collector.AttachPoint;
import org.moeaframework.analysis.collector.Collector;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.preferanto.core.PreferantoContext;
import org.preferanto.experiment.util.ContextListBuilder;
import org.preferanto.poset.Poset;
import org.preferanto.poset.PosetProvider;

public class ImpactCollector implements Collector {
	public static final String KEY = "Impact";

	private final Algorithm algorithm;
	private final PosetProvider posetProvider;
	private final Map<Integer, List<PreferantoContext>> nfeContextMap;
	private final Map<Integer, List<Solution>> nfeSolutionMap;
	private final int[] refNfes;

	private final ContextListBuilder ctxListBuilder = new ContextListBuilder();

	private int currRefStep = -1;

	public ImpactCollector(ReferenceSetReader referenceSetReader) throws IOException {
		this(null, referenceSetReader, null);
	}
	
	public ImpactCollector(ReferenceSetReader referenceSetReader, int[] refNfes) throws IOException {
		this(null, referenceSetReader, refNfes);
	}
	
	protected ImpactCollector(Algorithm algorithm, ReferenceSetReader referenceSetReader, int[] refNfes) throws IOException {
		this.algorithm = algorithm;
		this.refNfes = (refNfes != null) ? refNfes : referenceSetReader.getNfes();
		if(this.refNfes.length == 0) {
			throw new IllegalArgumentException("refNfes is empty");
		}
		this.posetProvider = referenceSetReader.getPosetProvider();
		this.nfeContextMap  = new TreeMap<>();
		this.nfeSolutionMap  = new TreeMap<>();
		for(int refNfe : this.refNfes) {
			List<double[]> objectiveValues = referenceSetReader.readObjectiveValues(refNfe);
			List<Solution> solutions = new ArrayList<>();
			for(double[] values : objectiveValues) {
				solutions.add(new Solution(values));
			}
			nfeSolutionMap.put(refNfe, solutions);
			List<PreferantoContext> ctxList = ctxListBuilder.createContextList(objectiveValues.toArray(new double[0][]));
			nfeContextMap.put(refNfe, ctxList);
		}
	}

	protected ImpactCollector(Algorithm algorithm, PosetProvider posetProvider, 
			Map<Integer, List<PreferantoContext>> nfeContextMap, Map<Integer, List<Solution>> nfeSolutionMap, int[] refNfes) {
		this.algorithm = algorithm;
		this.posetProvider = posetProvider;
		this.nfeContextMap = nfeContextMap;
		this.nfeSolutionMap = nfeSolutionMap;
		this.refNfes = refNfes;
		if(this.refNfes.length == 0) {
			throw new IllegalArgumentException("refNfes is empty");
		}
	}
	
	public int[] getRefNfes() {
		return refNfes;
	}
	
	@Override
	public AttachPoint getAttachPoint() {
		return AttachPoint.isSubclass(Algorithm.class).and(
				AttachPoint.not(AttachPoint.isNestedIn(Algorithm.class)));
	}

	@Override
	public Collector attach(Object object) {
		return new ImpactCollector((Algorithm)object, posetProvider, nfeContextMap, nfeSolutionMap, refNfes);
	}


	private final DominanceComparator paretoDominanceComparator = new ParetoDominanceComparator();
	/**
	 * 
	 * @param result
	 * @param refSolutions
	 * @return null if result contains a solution that dominates one from refSolutions. 
	 *         Otherwise, the list of indexes of solutions from result that are dominated by at least one reference solution.
	 */
	private List<Integer> getParetoNondominatedIndexes(NondominatedPopulation result, List<Solution> refSolutions) {
		List<Integer> nondominatedIndexes = new ArrayList<>();
		for(int i=0; i<result.size(); i++) {
			Solution sol = result.get(i);
			boolean dominated = false;
			for(Solution refSol : refSolutions) {
				int cmp = paretoDominanceComparator.compare(sol, refSol);
				if(cmp < 0) {
					// Found a solution that dominates a reference solution
					return null;
				}
				if(cmp > 0) {
					dominated = true;
					break;
				}
			}
			if(!dominated) {
				nondominatedIndexes.add(i);
			}
		}
		return nondominatedIndexes;
	}
	
	@Override
	public void collect(Accumulator accumulator) {
		NondominatedPopulation result = algorithm.getResult();
		
		List<PreferantoContext> solContexts = new ArrayList<>();
		for (Solution solution : result) {
			double[] objectives = solution.getObjectives();
			PreferantoContext ctx = ctxListBuilder.createContext(objectives);
			solContexts.add(ctx);
		}
		for(int refStep = currRefStep + 1; refStep < refNfes.length; refStep++) {
//			List<PreferantoContext> contexts = new ArrayList<>(solContexts);

			int nfe = refNfes[refStep];
			
			List<Solution> refSolutions = nfeSolutionMap.get(nfe);
			List<Integer> nondominatedIndexes = getParetoNondominatedIndexes(result, refSolutions);
			if(nondominatedIndexes == null) {
				currRefStep = refStep;
			} else {
				List<PreferantoContext> contexts = new ArrayList<>();
				for(int idx : nondominatedIndexes) {
					contexts.add(solContexts.get(idx));
				}
				int solCount = nondominatedIndexes.size();
				
				List<PreferantoContext> refContexts = nfeContextMap.get(nfe);
				contexts.addAll(refContexts);
				Poset poset = posetProvider.getPoset(contexts);
				int[][] matrix = poset.getRuleMatrix();
				int refCount = refContexts.size();
				for(int i=0; i<solCount; i++) {
					boolean dominated = false;
					for(int j=solCount; j < solCount + refCount; j++) {
						if(matrix[i][j] < 0) {
							dominated = true;
							break;
						}
					}
					if(!dominated) {
						currRefStep = refStep;
						break;
					}
				}
			}
			if(currRefStep < refStep) {
				break;
			}
		}		
		accumulator.add(KEY, currRefStep);
		
		if(currRefStep >= refNfes.length - 1) {
			algorithm.terminate();
		}
	}

}
