package org.preferanto.experiment.selectivity;

import java.util.ArrayList;
import java.util.List;

import org.preferanto.core.PreferantoContext;
import org.preferanto.poset.Poset;
import org.preferanto.poset.PosetProvider;

public class SelectivityProviders {
	/** SelectivityProvider based on the number of non-dominated solutions */
	public static SelectivityProvider PROVIDER_NON_DOMINATION = new SelectivityProvider() {
		@Override public String toString() {return "PROVIDER_NON_DOMINATION";}
		@Override public String getShortName() {return "domination";}

		@Override
		public double getSelectivity(List<PreferantoContext> contexts, PosetProvider posetProvider) {
			Poset poset = posetProvider.getPoset(contexts);
			int count = contexts.size();
			int nonDominatedCount = 0;
			for(int i=0; i<count; i++) {
				boolean nonDominated = true;
				for(int j=0; j<count; j++) {
					if(poset.getRule(i, j) < 0) {
						nonDominated = false;
						break;
					}
				}
				if(nonDominated) nonDominatedCount++;
			}
			double selectivity = 1.0 - (double)nonDominatedCount / (double)count;
			return selectivity;
		}		
	};
	
	/** SelectivityProvider based on the number of preference relations between solutions */
	public static SelectivityProvider PROVIDER_PREF_REL = new SelectivityProvider() {
		@Override public String toString() {return "PROVIDER_PREF_REL";}
		@Override public String getShortName() {return "relation";}

		@Override
		public double getSelectivity(List<PreferantoContext> contexts, PosetProvider posetProvider) {
			Poset poset = posetProvider.getPoset(contexts);
			int count = contexts.size();
			int prefRelCount = 0;
			for(int i=0; i<count-1; i++) {
				for(int j=i+1; j<count; j++) {
					if(poset.getRule(i, j) != 0) {
						prefRelCount++;
					}
				}
			}
			int totalRelCount = count * (count - 1) / 2;
			double selectivity = (double)prefRelCount / (double)totalRelCount;
			return selectivity;
		}
	};
	

	/** SelectivityProvider based on the number of non-dominated fronts */
	public static SelectivityProvider PROVIDER_FRONT_COUNT = new SelectivityProvider() {
		@Override public String toString() {return "PROVIDER_FRONT_COUNT";}
		@Override public String getShortName() {return "front";}

		@Override
		public double getSelectivity(List<PreferantoContext> contexts, PosetProvider posetProvider) {
			Poset poset = posetProvider.getPoset(contexts);
			int count = contexts.size();

			// dominationCount[i] = number of solutions that dominate solution i
			int[] dominationCount = new int[count];
			
			// dominationLists[i] = the list of solutions dominated by solution i  
			@SuppressWarnings("unchecked")
			List<Integer>[] dominationLists = (List<Integer>[])new List<?>[count];
			for(int i=0; i<count; i++) {
				dominationLists[i] = new ArrayList<>();
			}

			for(int i=0; i<count-1; i++) {
				for(int j=i+1; j<count; j++) {
					int rule = poset.getRule(i, j);
					if(rule > 0) {
						dominationLists[i].add(j);
						dominationCount[j]++;
					} else if(rule < 0) {
						dominationLists[j].add(i);
						dominationCount[i]++;
					}
				}
			}
			List<Integer>currentFront = new ArrayList<>();
			for(int i=0; i<count; i++) {
				if(dominationCount[i] == 0) {
					currentFront.add(i);
				}
			}
			
			int frontCount = 1;
			while(true) {
				List<Integer> newFront = new ArrayList<>();
				for(int i : currentFront) {
					for(int j : dominationLists[i]) {
						if(--dominationCount[j] == 0) {
							newFront.add(j);
						}
					}
				}
				if(newFront.isEmpty()) break;
				currentFront = newFront;
				frontCount++;
			}
			
			return (double)(frontCount - 1) / (double)count;
		}
	};
	
	private static final SelectivityProvider[] PROVIDERS = {PROVIDER_NON_DOMINATION, PROVIDER_PREF_REL, PROVIDER_FRONT_COUNT};
	
	public static SelectivityProvider getForName(String shortName) {
		for(SelectivityProvider provider : PROVIDERS) {
			if(provider.getShortName().equals(shortName)) {
				return provider;
			}
		}
		throw new IllegalArgumentException("No SelectivityProvider found for name '" + shortName + "'");
	}
}
