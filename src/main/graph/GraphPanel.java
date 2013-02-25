package graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;

import org.apache.commons.collections15.Transformer;

import bdd.BddModel;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.DefaultParallelEdgeIndexFunction;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.AbstractEdgeShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.ConstantDirectionalEdgeValueTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape.IndexedRendering;
import edu.uci.ics.jung.visualization.decorators.EdgeShape.Loop;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;

public class GraphPanel extends
		VisualizationViewer<MutableVertex, WeightedEdge> {

	private BddModel model;

	public GraphPanel(final Layout<MutableVertex, WeightedEdge> layout,
			BddModel bddModel) {
		super(layout);
		this.model = bddModel;
		setPreferredSize(new Dimension(model.getCanvasWidth(),
				model.getCanvasHeight()));
		getRenderContext().setVertexDrawPaintTransformer(
				new MyVertexDrawPaintFunction());
		getRenderContext().setVertexFillPaintTransformer(
				new MyVertexFillPaintFunction());
		getRenderContext().setEdgeDrawPaintTransformer(
				new MyEdgePaintFunction());
		getRenderContext().setEdgeStrokeTransformer(new MyEdgeStrokeFunction());
		getRenderContext().setVertexLabelTransformer(
				new MutableVertexLabeller<MutableVertex>());
		getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
		getRenderContext().setVertexShapeTransformer(new Transformer<MutableVertex, Shape>() {
			Shape rect = new Rectangle2D.Float(-10,-10,20,20);
			Shape ellipse = new Ellipse2D.Float(-10,-10,20,20);
			Shape root = new Ellipse2D.Float(-2, -2, 4, 4);
			
			@Override
			public Shape transform(MutableVertex v) {
				if(v.isRoot()) {
					return root;
				}
				else if(v.isTerminal()) {
					return rect;
 				}
				else {
					return ellipse;
				}
			}
		});
		
		
		
		getRenderContext().setEdgeLabelTransformer(
				new ToStringLabeller<WeightedEdge>());
		getRenderContext()
				.setEdgeLabelClosenessTransformer(
						new ConstantDirectionalEdgeValueTransformer<MutableVertex, WeightedEdge>(
								1.0, 0.5));

//		EdgeShape.Box<MutableVertex, WeightedEdge> shape = new EdgeShape.Box<MutableVertex, WeightedEdge>();
//		shape.setEdgeIndexFunction(DefaultParallelEdgeIndexFunction.<MutableVertex, WeightedEdge> getInstance());
		MyQuadCurve<MutableVertex, WeightedEdge> shape = new MyQuadCurve<MutableVertex, WeightedEdge>();
		shape.setEdgeIndexFunction(DefaultParallelEdgeIndexFunction.<MutableVertex, WeightedEdge> getInstance());
		getRenderContext().setEdgeShapeTransformer(shape);

		setGraphMouse(new DefaultModalGraphMouse<String, Number>());
		setBackground(Color.white);
	}

	/**
	 * @author danyelf
	 */
	public class MyEdgePaintFunction implements
			Transformer<WeightedEdge, Paint> {

		public Paint transform(WeightedEdge e) {
			return Color.BLACK;
		}
	}

	public class MyEdgeStrokeFunction implements
			Transformer<WeightedEdge, Stroke> {
		protected final Stroke THIN = new BasicStroke(1);
		protected final Stroke THICK = new BasicStroke(1);

		public Stroke transform(WeightedEdge e) {
			return THICK;
		}
	}

	public class MyVertexDrawPaintFunction implements
			Transformer<MutableVertex, Paint> {

		public Paint transform(MutableVertex v) {
			return Color.black;
		}
	}

	public class MyVertexFillPaintFunction implements
			Transformer<MutableVertex, Paint> {

		public Paint transform(MutableVertex v) {
			return v.findColor();
		}
	}

	public static class MyQuadCurve<V, E> extends
			AbstractEdgeShapeTransformer<V, E> implements
			IndexedRendering<V, E> {

		@SuppressWarnings("rawtypes")
		private static Loop loop = new Loop();
		/**
		 * singleton instance of the QuadCurve shape
		 */
		private static QuadCurve2D instance = new QuadCurve2D.Float();

		protected EdgeIndexFunction<V, E> parallelEdgeIndexFunction;

		@SuppressWarnings("unchecked")
		public void setEdgeIndexFunction(
				EdgeIndexFunction<V, E> parallelEdgeIndexFunction) {
			this.parallelEdgeIndexFunction = parallelEdgeIndexFunction;
			loop.setEdgeIndexFunction(parallelEdgeIndexFunction);
		}

		/**
		 * @return the parallelEdgeIndexFunction
		 */
		public EdgeIndexFunction<V, E> getEdgeIndexFunction() {
			return parallelEdgeIndexFunction;
		}

		/**
		 * Get the shape for this edge, returning either the shared instance or,
		 * in the case of self-loop edges, the Loop shared instance.
		 */
		@SuppressWarnings("unchecked")
		public Shape transform(Context<Graph<V, E>, E> context) {
			Graph<V, E> graph = context.graph;
			E e = context.element;
			Pair<V> endpoints = graph.getEndpoints(e);
			if (endpoints != null) {
				boolean isLoop = endpoints.getFirst().equals(
						endpoints.getSecond());
				if (isLoop) {
					return loop.transform(context);
				}
			}
			
//			int index = (int)(5.0f * new Random().nextDouble());
			int index = 1;
			if (parallelEdgeIndexFunction != null) {
				index = parallelEdgeIndexFunction.getIndex(graph, e);
				index = (index == 0) ? 1 : -1;
			}

			float controlY = 2 * control_offset_increment * index;
			
			instance.setCurve(0.0f, 0.0f, 0.5f, controlY, 1.0f, 0.0f);
			return instance;
		}
	}

}
