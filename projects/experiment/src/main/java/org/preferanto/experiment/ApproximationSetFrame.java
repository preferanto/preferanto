package org.preferanto.experiment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.preferanto.experiment.moea.MOEAUtils;

public class ApproximationSetFrame extends ApplicationFrame {
	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_DELAY = 100;

	private final String baseTitle;
	private final Accumulator[] accumulators;
	private final String[] seriesNames;
	private final JFreeChart chart;
	private final XYSeriesCollection resultDataset;

	private final JButton butFirst = new JButton("<<");
	private final JButton butPrev = new JButton("<");
	private final JButton butNext = new JButton(">");
	private final JButton butLast = new JButton(">>");
	private final JButton butPlay = new JButton("Play");
	private final JComboBox<Integer> cmbDelay = new JComboBox<>(new Integer[]{1, 2, 5, 10, 20, 50, 100, 200, 500, 1000});
	private final Timer playTimer;
	
	private final int probesCount;

	private int currentProbeIndex;

	public ApproximationSetFrame(String baseTitle, Accumulator[] accumulators, NondominatedPopulation refSet, String[] seriesNames) throws Exception {
		super(baseTitle);
		
		this.baseTitle = baseTitle;
		this.accumulators = accumulators;
		this.seriesNames = seriesNames;
		XYSeries refSeries = MOEAUtils.createSeries(seriesNames[0], refSet);
		XYDataset refDataset = new XYSeriesCollection(refSeries);
		chart = ChartFactory.createXYLineChart(
				baseTitle,                        // Title
				"z0",                         // X-Axis label
				"z1",                         // Y-Axis label
				refDataset,                   // Dataset
				PlotOrientation.HORIZONTAL,   // Orientation
				true,                         // Show legend
				true,                         // Show tooltips
				true                          // Show urls
				);
		
		
		int count = Integer.MAX_VALUE;
		for(int k=0; k < accumulators.length; k++) {
			int size = accumulators[k].size(MOEAUtils.APPROX_SET_KEY);
			if(size < count) {
				if(k > 0) {
					throw new IllegalArgumentException("probesCount changed from " + count + " to " + size);
				}
				count = size;
			}
		}
		if(count < 1) {
			throw new Exception("No probes");
		}
		probesCount = count;

		double[] minVal = {Double.MAX_VALUE, Double.MAX_VALUE};
		double[] maxVal = {-Double.MAX_VALUE, -Double.MAX_VALUE};

		for(Solution solution : refSet) {
			double[] objectives = solution.getObjectives();
			if(objectives.length < 2) {
				throw new IllegalArgumentException("Solution with " + objectives.length + " objectives: " + MOEAUtils.toString(solution));
			}
			for(int k=0; k<2; k++) {
				if(objectives[k] < minVal[k]) {
					minVal[k] = objectives[k];
				}
				if(objectives[k] > maxVal[k]) {
					maxVal[k] = objectives[k];
				}
			}
		}
	
		resultDataset = new XYSeriesCollection();
		configureSeries(0);
		
		XYItemRenderer renderer = new XYLineAndShapeRenderer(false, true);
		renderer.setSeriesPaint(1, Color.BLACK);
		
		XYPlot xyPlot = chart.getXYPlot();
		xyPlot.getDomainAxis().setRange(minVal[0], maxVal[0] * 1.1);
		xyPlot.getRangeAxis().setRange(minVal[1], maxVal[1] * 1.1);
		
		xyPlot.setRenderer(1, renderer);
		xyPlot.setDataset(1, resultDataset);

		
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setFillZoomRectangle(true);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setPreferredSize(new java.awt.Dimension(1024, 1024));
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(chartPanel, BorderLayout.CENTER);
        
        JPanel controlPanel = new JPanel(new BorderLayout());
        JPanel butPanel = new JPanel();
        controlPanel.add(butPanel, BorderLayout.CENTER);

        butPanel.add(butFirst);
        butPanel.add(butPrev);
        butPanel.add(butNext);
        butPanel.add(butLast);
        
        JPanel playPanel = new JPanel();
        playPanel.add(butPlay);
        JLabel lbDelay = new JLabel("  Delay: ");
        playPanel.add(lbDelay);
        cmbDelay.setSelectedItem(DEFAULT_DELAY);
        playPanel.add(cmbDelay);
        JLabel lbMs = new JLabel("ms.");
        playPanel.add(lbMs);
        
        controlPanel.add(playPanel, BorderLayout.EAST);
        
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        butFirst.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				configureSeries(0);				
			}
		});
        butLast.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				configureSeries(probesCount-1);				
			}
		});
        butPrev.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				configureSeries(currentProbeIndex - 1);
			}
		});
        butNext.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				configureSeries(currentProbeIndex + 1);
			}
		});
        
        playTimer = new Timer(DEFAULT_DELAY, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(currentProbeIndex >= probesCount) {
					playTimer.stop();
				} else {
					try {
						currentProbeIndex++;
						configureSeries(currentProbeIndex);
					} catch(Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
        
        cmbDelay.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				int idx = cmbDelay.getSelectedIndex();
				if(idx >= 0) {
					playTimer.setDelay(cmbDelay.getItemAt(idx));
				}
			}
		});
        butPlay.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				currentProbeIndex = 0;
				playTimer.start();
			}
		});
        
        
        setContentPane(mainPanel);
	}
	
	public void run() {
        pack();
        RefineryUtilities.centerFrameOnScreen(this);
        setVisible(true);	
	}

	private void configureSeries(int probeIndex) {
		if(probeIndex < 0 || probeIndex >= probesCount) return;
		currentProbeIndex = probeIndex;
		resultDataset.removeAllSeries();
		for(int k=0; k<accumulators.length; k++) {
			@SuppressWarnings("unchecked")
			List<Solution> result = (List<Solution>)accumulators[k].get(MOEAUtils.APPROX_SET_KEY, probeIndex);
			XYSeries series = MOEAUtils.createSeries(seriesNames[k+1], result);
			resultDataset.addSeries(series);
		}
		chart.setTitle(baseTitle + " (" + (currentProbeIndex + 1) + " / " + probesCount + ")");
		
		butFirst.setEnabled(currentProbeIndex > 0);
		butPrev.setEnabled(currentProbeIndex > 0);
		butNext.setEnabled(currentProbeIndex < probesCount - 1);
		butLast.setEnabled(currentProbeIndex < probesCount - 1);
	}
}
