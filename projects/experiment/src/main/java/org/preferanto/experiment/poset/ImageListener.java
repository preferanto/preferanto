package org.preferanto.experiment.poset;

import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.collections15.Transformer;
import org.preferanto.core.Utils;
import org.preferanto.experiment.poset.DefaultEdge.Style;
import org.preferanto.poset.Poset;
import org.preferanto.poset.Relation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;

public class ImageListener extends FileListener {
	private static final Logger log = LoggerFactory.getLogger(ImageListener.class);
	
	private final ImageConfiguration imgConfig;
	
	public ImageListener(ImageConfiguration imgConfig) {
		super(imgConfig);
		this.imgConfig = imgConfig;
	}
	
	private DirectedGraph<Integer, DefaultEdge> getGraph(Poset poset) {
		DirectedGraph<Integer, DefaultEdge> graph = new DirectedSparseGraph<>();
		int size = poset.getSize();
		int[][] matrix = poset.getRuleMatrix();
		for(int i=0; i<size; i++) {
			graph.addVertex(i);
			for(int j=0; j<size; j++) {
				int r = matrix[i][j];
				if(r > 0) {
					DefaultEdge edge = new DefaultEdge(r);
					graph.addEdge(edge, i, j);
				}
			}
		}
		return graph;
	}
	
	private DirectedGraph<Integer, DefaultEdge> getGraph(int rule, Poset poset, List<Relation> relations, double[] maxDiffs) {
		DirectedGraph<Integer, DefaultEdge> graph = getGraph(poset);
		for(Relation rel : relations) {
			DefaultEdge edge = graph.findEdge(rel.getIdxFrom(), rel.getIdxTo());
			if(edge == null) {
				edge = new DefaultEdge(rule);
				if(diffCompare(rel.getDiffs(), maxDiffs) < 0) {
					throw new AssertionError("Expected REMOVED");
				}
				edge.setStyle(Style.REMOVED);
				graph.addEdge(edge, rel.getIdxFrom(), rel.getIdxTo());
			} else {
				if(diffCompare(rel.getDiffs(), maxDiffs) < 0) {
					edge.setStyle(Style.PRESERVED);
				}
			}
		}
		return graph;
	}
	
	private int diffCompare(double[] diffs1, double[] diffs2) {
		for(int k=0; k<diffs1.length; k++) {
			int result = Utils.doubleCompare(diffs1[k], diffs2[k]);
			if(result != 0) return result;
		}
		return 0;
	}
	
	private void writeStream(OutputStream ostream, DirectedGraph<Integer, DefaultEdge> graph) {		
		try {
			if(!enabled || ostream == null) return;
			
			Layout<Integer, DefaultEdge> layout = new CircleLayout<>(graph);
			layout.setSize(new Dimension(imgConfig.getWidth(), imgConfig.getHeight()));
			BasicVisualizationServer<Integer, DefaultEdge> vv = new BasicVisualizationServer<>(layout);
			vv.setPreferredSize(new Dimension(imgConfig.getWidth(), imgConfig.getHeight()));
			
			VisualizationImageServer<Integer, DefaultEdge> vis = new VisualizationImageServer<>(vv.getGraphLayout(), vv.getGraphLayout().getSize());
			vis.setBackground(imgConfig.getBackgroundColor());

			RenderContext<Integer, DefaultEdge> renderContext = vis.getRenderContext();
			
			renderContext.setEdgeStrokeTransformer(new Transformer<DefaultEdge, Stroke>() {
				@Override public Stroke transform(DefaultEdge edge) {return edge.getStroke();}
			});
			
			renderContext.setEdgeDrawPaintTransformer(new Transformer<DefaultEdge, Paint>() {
				@Override public Paint transform(DefaultEdge edge) {return edge.getDraw();}
			});
			
			renderContext.setEdgeLabelTransformer(new ToStringLabeller<DefaultEdge>());
			renderContext.setEdgeShapeTransformer(new EdgeShape.BentLine<Integer, DefaultEdge>());
			renderContext.setVertexLabelTransformer(new ToStringLabeller<Integer>());
			vis.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);

			BufferedImage image = (BufferedImage) vis.getImage(
					new Point2D.Double(vv.getGraphLayout().getSize().getWidth() / 2, vv.getGraphLayout().getSize().getHeight() / 2), 
					new Dimension(vv.getGraphLayout().getSize()));

			ImageIO.write(image, imgConfig.getImgFormatName(), ostream);
		} catch(Exception e) {
			log.warn("writeStream() failed." + e);
		}
	}

	@Override
	public void getPosetStarted(Poset poset) {
		// Nothing to do
	}

	@Override
	public void ruleStarted(int rule, Poset poset) {
		// Nothing to do
	}

	@Override
	public void ruleFinished(int rule, Poset poset, List<Relation> relations, int relationsAdded) {
		double[] maxDiffs = (relationsAdded > 0) ? relations.get(relationsAdded - 1).getDiffs() : null;
		DirectedGraph<Integer, DefaultEdge> graph = getGraph(rule, poset, relations, maxDiffs);
		writeStream(getOutputStream(rule), graph);
	}

	@Override
	public void getPosetFinished(Poset poset) {
		DirectedGraph<Integer, DefaultEdge> graph = getGraph(poset);
		writeStream(getOutputStream(0), graph);
	}
	
}
