package org.preferanto.experiment.selectivity;

import java.util.Locale;

import org.preferanto.core.PreferantoContext;
import org.preferanto.core.PreferantoContextImpl;
import org.preferanto.experiment.util.RandomUtil;

public class DTLZ2RndParetoContextProvider implements ParetoContextProvider {
	private final int objCount;
	
	public DTLZ2RndParetoContextProvider(int objCount) {
		this.objCount = objCount;
	}

	@Override
	public PreferantoContext getNextContext() {
		double[] thetas = new double[objCount - 1];
		for(int k=0; k<objCount - 1; k++) {
			thetas[k] = RandomUtil.INSTANCE.nextDouble();
		}
		
		PreferantoContextImpl ctx = new PreferantoContextImpl();
		for(int i=0; i<objCount; i++) {
			double val = getObjVal(i, thetas);
			ctx.setDouble("z" + i, val);
		}
		return ctx;
	}

	private double getObjVal(int i, double[] thetas) {
		double val = (i == 0) ? 1.0 : Math.sin(thetas[objCount - i - 1]);
		for(int k=0; k < objCount - i - 1; k++) {
			val *= Math.cos(thetas[k]);
		}
		return val;
	}
	
	public String toString(PreferantoContext ctx) {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		double d = 0;
		for(int i=0; i<objCount; i++) {
			double val = ctx.getDouble("z" + i);
			d += val * val;
			sb.append(String.format(Locale.US, "%sz%d=%8f", sep, i, val));
			sep = ", ";
		}
		sb.append(" : d = " + d);
		return sb.toString();
	}
	
	public static void main(String[] args) {
		DTLZ2RndParetoContextProvider provider = new DTLZ2RndParetoContextProvider(3);
		for(int i=0; i<100; i++) {
			PreferantoContext ctx = provider.getNextContext();
			System.out.printf(Locale.US, "ctx-%04d: %s\n", i, provider.toString(ctx));
		}
	}
}
