package org.preferanto.experiment.selectivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jfree.data.xy.XYSeries;
import org.preferanto.core.PreferantoContext;
import org.preferanto.core.PreferantoException;
import org.preferanto.experiment.ui.NavigableFrame;
import org.preferanto.experiment.ui.NavigableFrame.ProbeSet;
import org.preferanto.poset.Poset;
import org.preferanto.poset.PosetProvider;

public class DTLZ2SelectivityViewer {
	private final NavigableFrame frame;
	private final PosetProvider posetProvider;

	public DTLZ2SelectivityViewer(int ruleCount, int tupleSize, double[] alphas) {
		try {
			PrefGen prefGen = new PrefGen(2, ruleCount, tupleSize, alphas);
			String preferantoText = prefGen.getPreferanto();
			posetProvider = new PosetProvider(preferantoText);
		} catch(Throwable t) {
			throw new PreferantoException(t);
		}
		frame = new NavigableFrame("ruleCount: " + ruleCount + ", tupleSize: " + tupleSize + ", alphas: " + Arrays.toString(alphas));
	}

	public void add(int paretoSize) {
		System.out.println("Adding series " + paretoSize + "...");
		ParetoContextProvider ctxProvider = new DTLZ2RndParetoContextProvider(2);
		List<PreferantoContext> contexts = new ArrayList<>();
		for(int k=0; k<paretoSize; k++) {
			contexts.add(ctxProvider.getNextContext());				
		}
		Poset poset = posetProvider.getPoset(contexts);

		XYSeries refSeries = new XYSeries("ref-" + paretoSize);
		XYSeries prefSeries = new XYSeries("pref-" + paretoSize);
		for(int i=0; i<paretoSize; i++) {
			boolean nonDominated = true;
			for(int j=0; j<paretoSize; j++) {
				if(poset.getRule(i, j) < 0) {
					nonDominated = false;
					break;
				}
			}
			PreferantoContext ctx = contexts.get(i);
			double z0 = ctx.getDouble("z0");
			double z1 = ctx.getDouble("z1");
			refSeries.add(z0, z1);
			if(nonDominated) {
				prefSeries.add(z0, z1);
			}
		}
		
		
		ProbeSet probeSet = new ProbeSet("size: " + paretoSize, refSeries, prefSeries);
		frame.addProbeSet(probeSet);
	}
	
	public void run() {
		frame.run();
	}
	
	public static void main(String[] args) {
		DTLZ2SelectivityViewer viewer = new DTLZ2SelectivityViewer(2, 2, new double[] {1, 0.3});
		for(int paretoSize : new int[] {50, 100, 150, 200, 300, 400, 500, 1000}) {
			viewer.add(paretoSize);
		}
		viewer.run();
	}
}
