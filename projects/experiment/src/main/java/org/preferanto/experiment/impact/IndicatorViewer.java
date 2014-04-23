package org.preferanto.experiment.impact;

import java.awt.Color;
import java.io.IOException;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.preferanto.experiment.ui.NavigableFrame;
import org.preferanto.experiment.ui.NavigableFrame.ProbeSet;

public class IndicatorViewer {
	private final NavigableFrame frame;
	private final int[] nfes;
	private final String indicatorDir;
	private final IndicatorReader indicatorReader; 

	public IndicatorViewer(String baseDir, String selProviderName, int... nfes) {
		this.frame = new NavigableFrame(selProviderName);
		this.nfes = nfes;
		this.indicatorDir = baseDir + "/" + selProviderName;
		this.indicatorReader = new IndicatorReader(indicatorDir);
		
		XYPlot xyPlot = frame.getChart().getXYPlot();
		xyPlot.getDomainAxis().setLabel("Selectivity");
		xyPlot.getRangeAxis().setLabel("Nondominance rate");
//		xyPlot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
//		xyPlot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
		
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
		
		for(int i=0; i<nfes.length; i++) {
//			renderer.setSeriesLinesVisible(i, true);
//			renderer.setSeriesShape(i, new java.awt.geom.Line2D.Double());
			
//			renderer.setSeriesPaint(i, Color.BLACK);
			
//			xyPlot.getDomainAxis().setRange(0, 1);
//			xyPlot.getRangeAxis().setRange(0, 1);
			
			xyPlot.setRenderer(i, renderer);
		}
		
	}

	public void add(int objCount) throws IOException {
		System.out.println("Adding bjCount " + objCount + "...");

		XYSeries[] series = new XYSeries[nfes.length] ;
		
		for(int k = 0; k < nfes.length; k++) {
			int nfe = nfes[k];
			series[k] = new XYSeries("ind-" + nfe);
			
			double[] values = indicatorReader.getIndicatorMap(objCount).getValues(nfe);
			for(int i=1; i<values.length; i++) {
				double selectivity = 0.1 * i;
				series[k].add(selectivity, values[i]);
			}
		}
		ProbeSet probeSet = new ProbeSet("objCount: "+ objCount, series);
		frame.addProbeSet(probeSet);
	}
	
	public void run() {
		frame.run();
	}
	
	public static void main(String[] args) throws IOException {
		IndicatorViewer viewer = new IndicatorViewer("results/indicator", "domination", 10000);
		for(int objCount = 2; objCount <= 10; objCount++) {
			viewer.add(objCount);
		}
		viewer.run();
	}
}
