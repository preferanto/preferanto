package org.preferanto.experiment.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class NavigableFrame extends ApplicationFrame {
	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_DELAY = 100;

	private final String baseTitle;
	private final List<ProbeSet> probeSets = new ArrayList<>();
	private final JFreeChart chart;
	private final XYSeriesCollection resultDataset;

	private final JButton butFirst = new JButton("<<");
	private final JButton butPrev = new JButton("<");
	private final JButton butNext = new JButton(">");
	private final JButton butLast = new JButton(">>");
	private final JButton butPlay = new JButton("Play");
	private final JComboBox<Integer> cmbDelay = new JComboBox<>(new Integer[]{1, 2, 5, 10, 20, 50, 100, 200, 500, 1000});
	private final Timer playTimer;
	
	private int currentProbeIndex;

	private boolean autoAxisRange = true;
	private boolean probeSetsChanged = true;

	public static class ProbeSet {
		private final String title;
		private final XYSeries[] series;
		private final double minXVal;
		private final double maxXVal;
		private final double minYVal;
		private final double maxYVal;

		public ProbeSet(String title, XYSeries... series) {
			this.title = title;
			this.series = series;
			
			double minX = Double.MAX_VALUE;
			double minY = Double.MAX_VALUE;
			double maxX = -Double.MAX_VALUE;
			double maxY = -Double.MAX_VALUE;
			for(XYSeries s : series) {
				if(s.getMinX() < minX) minX = s.getMinX();
				if(s.getMinY() < minY) minY = s.getMinY();
				if(s.getMaxX() > maxX) maxX = s.getMaxX();
				if(s.getMaxY() > maxY) maxY = s.getMaxY();
			}
			minXVal = minX;
			maxXVal = maxX;
			minYVal = minY;
			maxYVal = maxY;
		}

		public String getTitle() {
			return title;
		}
		
		public XYSeries[] getSeries() {
			return series;
		}
		
		public double getMinXVal() {
			return minXVal;
		}
		public double getMinYVal() {
			return minYVal;
		}
		public double getMaxXVal() {
			return maxXVal;
		}
		public double getMaxYVal() {
			return maxYVal;
		}
	}
	
	public NavigableFrame(String baseTitle) {
		super(baseTitle);
		
		this.baseTitle = baseTitle;
		XYDataset refDataset = new XYSeriesCollection();
		chart = ChartFactory.createXYLineChart(
				baseTitle,                        // Title
				"z0",                         // X-Axis label
				"z1",                         // Y-Axis label
				refDataset,                   // Dataset
				PlotOrientation.VERTICAL,     // Orientation
				true,                         // Show legend
				true,                         // Show tooltips
				true                          // Show urls
				);
		
		
	
		resultDataset = new XYSeriesCollection();
		configureSeries(0);
		
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
		
		renderer.setSeriesLinesVisible(0, true);
		renderer.setSeriesShape(0, new java.awt.geom.Line2D.Double());
		
		renderer.setSeriesPaint(1, Color.BLACK);
		
		XYPlot xyPlot = chart.getXYPlot();
//		xyPlot.getDomainAxis().setRange(minVal[0], maxVal[0] * 1.1);
//		xyPlot.getRangeAxis().setRange(minVal[1], maxVal[1] * 1.1);
		
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
				configureSeries(probeSets.size()-1);				
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
				if(currentProbeIndex >= probeSets.size()) {
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
	
	public JFreeChart getChart() {
		return chart;
	}
	
	public XYLineAndShapeRenderer getRenderer() {
		return (XYLineAndShapeRenderer)chart.getXYPlot().getRenderer();
	}
	
	private void updateAxisRanges() {
		if(autoAxisRange) {
			double minX = Double.MAX_VALUE;
			double minY = Double.MAX_VALUE;
			double maxX = -Double.MAX_VALUE;
			double maxY = -Double.MAX_VALUE;
			for(ProbeSet probeSet : probeSets) {
				if(probeSet.getMinXVal() < minX) minX = probeSet.getMinXVal();
				if(probeSet.getMinYVal() < minY) minY = probeSet.getMinYVal();
				if(probeSet.getMaxXVal() > maxX) maxX = probeSet.getMaxXVal();
				if(probeSet.getMaxYVal() > maxY) maxY = probeSet.getMaxYVal();
			}
			XYPlot xyPlot = chart.getXYPlot();
			xyPlot.getDomainAxis().setRange(minX, maxX * 1.1);
			xyPlot.getRangeAxis().setRange(minY, maxY * 1.1);

			probeSetsChanged = false;
		}
	}
	
	public boolean isAutoAxisRange() {
		return autoAxisRange;
	}
	public void setAutoAxisRange(boolean autoAxisRange) {
		probeSetsChanged = true;
		this.autoAxisRange = autoAxisRange;
	}
	
	public void run() {
		updateAxisRanges();
		configureSeries(0);
        pack();
        RefineryUtilities.centerFrameOnScreen(this);
        setVisible(true);	
	}

	public boolean addProbeSet(ProbeSet probeSet) {
		probeSetsChanged = true;
		return probeSets.add(probeSet);
	}
	
	private void configureSeries(int probeIndex) {
		if(probeIndex < 0 || probeIndex >= probeSets.size()) return;
		if(probeSetsChanged) {
			updateAxisRanges();
		}
		currentProbeIndex = probeIndex;
		resultDataset.removeAllSeries();
		ProbeSet probeSet = probeSets.get(probeIndex);
		for(XYSeries series : probeSet.getSeries()) {
			resultDataset.addSeries(series);
		}
		chart.setTitle(baseTitle + " (" + probeSet.getTitle() + ")");
		
		butFirst.setEnabled(currentProbeIndex > 0);
		butPrev.setEnabled(currentProbeIndex > 0);
		butNext.setEnabled(currentProbeIndex < probeSets.size() - 1);
		butLast.setEnabled(currentProbeIndex < probeSets.size() - 1);
	}
}
