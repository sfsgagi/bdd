package bdd

import edu.uci.ics.jung.algorithms.layout.Layout
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance.SourceData;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph 
import edu.uci.ics.jung.graph.Graph
import edu.uci.ics.jung.samples.ShortestPathDemo.MyEdgePaintFunction;
import edu.uci.ics.jung.samples.ShortestPathDemo.MyEdgeStrokeFunction;
import edu.uci.ics.jung.samples.ShortestPathDemo.MyVertexDrawPaintFunction;
import edu.uci.ics.jung.samples.ShortestPathDemo.MyVertexFillPaintFunction;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane 
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import graph.BddHelper 
import graph.BddTreeLayout;
import graph.GraphChange;
import graph.GraphPanel 
import graph.MutableVertex 
import graph.WeightedEdge 
import java.awt.BorderLayout 
import java.awt.Color;
import java.awt.Point;
import java.util.Map;

import org.pushingpixels.trident.Timeline 
import org.pushingpixels.trident.Timeline.RepeatBehavior 
import org.pushingpixels.trident.TimelinePropertyBuilder.PropertySetter 


class BddController {
    // these will be injected by Griffon
    def model
    def view
	
	GraphZoomScrollPane graphScroll
	
	def v1

	void mvcGroupInit(Map args) {
		model.currentStep = -1
	}

    // void mvcGroupDestroy() {
    //    // this method is called when the group is destroyed
    // }

	// TODO treba odraditi obelezavanje ivica
	// TODO prebaciti na laptop i instalacija
	
	def openFunctionDialog = {
		app.views.FunctionDialog.functionDialog.visible = true
	}
	
	def refresh() {
		view.mainPanel.revalidate()
		view.mainPanel.repaint()
	}
	
	def findReorderedIndex = { index, order ->
		def nLevels = order.size()
		String str = Integer.toString (index, 2).padLeft(nLevels, '0')
		def orderIndices = order.collect { Integer.parseInt (it[1]) }
		
		char[] finalStr = Integer.toString (index, 2).padLeft(nLevels, '0').getChars()
		(0..nLevels - 1).each { int i ->
			finalStr[orderIndices[i] - 1] = str[i]
		}
		println "Final string: $finalStr"
    def binary = Integer.parseInt(finalStr as String, 2)
    println "returning value: $binary"
    // TODO proveri tip ovog binary
    binary.inspect()
    return binary
		//return Integer.parseInt(finalStr as String, 2)
	}
	
	def redraw(functionModel, nLevels) {
		model.functionDefined = true
		model.currentStep = -1
		def order = view.pnlReorder.order
		println "ORDER KOJI KORISTIMO: $order"
		def vertices = []
		def fVertex = new MutableVertex("F", MutableVertex.ROOT)
		order.eachWithIndex { it, i ->
			(1..2**i).each {
				vertices << new MutableVertex(order[i])
			}
		}
		
		(0..2**nLevels - 1).each { it ->
      println "debugging..."
      println it
      println order
			int index = findReorderedIndex(it, order)
      println "Stampanje indeksa: " + index
      println "functionModel: " + functionModel[index]
			vertices << new MutableVertex(functionModel[index].f ? "1" : "0", MutableVertex.TERMINAL) 
		}
		
		//println vertices.dump()
				
		def edges = []
		edges << [edge: new WeightedEdge(2), source: fVertex, dest: vertices[0] ]
		(0..2**nLevels - 2).each { i ->
			def startIndex = i * 2 + 1
			edges << [edge: new WeightedEdge(0), source: vertices[i], dest: vertices[startIndex]]
			edges << [edge: new WeightedEdge(1), source: vertices[i], dest: vertices[startIndex + 1]]
		}
		
		//println edges.dump()
		redrawGraph(edges);
	}
	
	def variablesReordered() {
		model.currentStep = -1 
		model.graphDrawn = false
//		redraw(app.models.FunctionDialog.tableModel, view.pnlReorder.order.size())
	}
	
	def redrawReordered() {
		redraw(app.models.FunctionDialog.tableModel, view.pnlReorder.order.size())
	}
	
	def redrawGraph(graphData) {
		
		Graph<MutableVertex, String> g = new DirectedOrderedSparseMultigraph<MutableVertex, String>();
		graphData.each {
			g.addEdge(it.edge, it.source, it.dest)
		}
		
		Layout<MutableVertex, String> layout = null;
		int nLevels = app.controllers.FunctionDialog.view.cmbNVariables.selectedItem
		layout = new BddTreeLayout<MutableVertex, String>(nLevels, g, 50, 100);
		
//		model.canvasWidth = view.mainScrollPane.width - 20
//		model.canvasHeight = view.mainScrollPane.height - 20
		//layout.setSize([model.canvasWidth - 20, model.canvasHeight - 20] as Dimension); // sets the initial size of the space
		
		def graphPanel = new GraphPanel(layout, model)
		g.vertices.each { v ->
			v.color = null
		}
		
		def transformer = model?.graphPanel?.getRenderContext()?.getMultiLayerTransformer();
		if(transformer != null) {
			graphPanel.getRenderContext().setMultiLayerTransformer(transformer);
		}

		graphScroll = new GraphZoomScrollPane(graphPanel)
		
		
		
		model.graph = g
		model.layout = layout
		model.graphPanel = graphPanel
		model.graphDrawn = true
		doLater {
			view.mainPanel.removeAll()
			view.mainPanel.add(graphScroll, BorderLayout.CENTER)
			view.mainPanel.revalidate()
			
		}
	}
	
	def reduceGraph = { 
		println "BEFORE REDUCE**********************"
		model.steps = []
		def graph = model.graph
		
		def reducedEdges = reduceSubtrees(graph);
		reducedEdges = reduceParallelEdges(reducedEdges)
		reducedEdges = reduceExtraTerminals(reducedEdges)
		
		
		println "FFFFFINAL:"
				
		redrawGraph(reducedEdges)
		def vertices = model.graph.vertices
		def edges = model.graph.edges
		addStep(vertices, edges, reducedEdges, model.graph, GraphChange.finished())
		model.nSteps = model.steps.size()
		model.currentStep = model.nSteps - 2
		updateStatus()
		return [vertices, reducedEdges]	
	}
	
	def reduceSubtrees = { graph ->
		def reducedVertices = new ArrayList(graph.vertices)
		def reducedEdges = []
		def edges = new ArrayList(graph.edges)
		addStep(reducedVertices, edges, reducedEdges, graph, GraphChange.empty())
				
		int i = 0
		def parsedVertices = []
		while(i < reducedVertices.size()) {
			def vertex = reducedVertices[i]
			if(!vertex.isTerminal() && !vertex.isRoot() && !parsedVertices.contains(vertex)) {
				def sameLevelVertices = reducedVertices.findAll { v ->
					v.name == vertex.name && !parsedVertices.contains(v)
				}
				parsedVertices.addAll(sameLevelVertices)
				
				def outEdges = []
				sameLevelVertices.each { v -> outEdges.addAll(graph.getOutEdges(v)) }
				def subtreeVertices = outEdges.collect { e -> graph.getDest(e)}
				def values = subtreeVertices.collect { v -> getSubtreeValues(v, graph) }
				
				// sequence, list of subtrees
				def sameValues = [:]
				
				// init with lists
				values.each { val ->
					sameValues[val] = []
				}
				
				values.eachWithIndex{ val, ind ->
					sameValues[val] << subtreeVertices[ind]
				}
				println "SUBTREE MAP:" + sameValues
				sameValues.each { k, val ->
					if(val.size() > 1) {
						// subtree to redirect to
						def subtreeRoot = val[0] 
						
						(1..val.size() - 1).each {
							def v = val[it]
							// remove subtree
							def subtree = BddHelper.getSubTree(graph, v)
							
							reducedVertices -= subtree.getVertices()
							parsedVertices.addAll(subtree.getVertices())
							edges -= subtree.getEdges()
							
							println "****REMOVED VERTICES: " + subtree.getVertices()
							println "****REMOVED EDGES: " + subtree.getEdges()
							
							
							//addStep(reducedVertices, edges, reducedEdges, graph, GraphChange.expelled(subtree))
							
							// redirect to other subtree
							def edgeToRedirect = graph.getInEdges(v).iterator().next()
							edges -= edgeToRedirect
							reducedEdges << [edge: edgeToRedirect, source: graph.getSource(edgeToRedirect), dest: subtreeRoot]
							
//							addStep(reducedVertices, edges, reducedEdges, graph,
//								GraphChange.redirected(edgeToRedirect, v, subtreeRoot) )
							
							addStep(reducedVertices, edges, reducedEdges, graph,
								GraphChange.all(subtree.vertices, subtree.edges, edgeToRedirect, v, subtreeRoot))
							
							//println "RV:" + reducedVertices
							println " REDUCED EDGES:" + reducedEdges
							//println " EDGES:" + reducedEdges
						}
					}
				}
			}
			i++;
		}
		reducedEdges.addAll(edges.collect { [edge: it, source: graph.getSource(it), dest: graph.getDest(it)] })
		return reducedEdges
	}
	
	def reduceParallelEdges = { edges ->
		def graph = createHelperGraph(edges)
		
		while(hasParallelsToReduce(graph)) {
			graph = reduceSingleParallelEdge(graph)
		}
		def reducedEdges = []
		def finalEdges = new ArrayList(graph.edges)
		reducedEdges.addAll(finalEdges.collect { [edge: it, source: graph.getSource(it), dest: graph.getDest(it)] })
		
		return reducedEdges
	}
	
	def reduceSingleParallelEdge = { graph ->
		def reducedVertices = new ArrayList(graph.vertices)
		def reducedEdges = []
		def edges = new ArrayList(graph.edges)
//		println "V: " + reducedVertices
//		println "E: " + edges
		// reduce parallel edges
		
		def vertex = graph.vertices.find { v ->
			if(!v.isTerminal()) {
				def outEdges = graph.getOutEdges(v)
				def subtreeVertices = outEdges.collect { graph.getDest(it)}
				
				return subtreeVertices[0] == subtreeVertices[1]
			} else {
				return false
			}
		}
		
		
		
		if(!vertex.isTerminal()) {
			def outEdges = graph.getOutEdges(vertex)
			def subtreeVertices = outEdges.collect { graph.getDest(it)}
			println ".............................VERTEX: " + vertex
			println ".............................OE: " + outEdges
			println ".............................SUBTREE VERTICES: " + subtreeVertices
 
			
			if(subtreeVertices[0] == subtreeVertices[1]) {
				reducedVertices -= vertex
				edges -= outEdges
				
				def edgesToRedirect = graph.getInEdges(vertex)
				edges -= edgesToRedirect
				edgesToRedirect.each { e ->
					def parentVertex = graph.getSource(e)
					if(subtreeVertices[0] == null) {
						println "GRESKA!!!!"
						println subtreeVertices.size()
						println vertex
						println outEdges
						println edgeToRedirect
						println subtreeVertices
						println "_____________________________________________________________"
					}
					reducedEdges << [edge: e, source: parentVertex, dest: subtreeVertices[0] ]
	                addStep(reducedVertices, edges, reducedEdges, graph,
						GraphChange.all([vertex], outEdges, e, vertex, subtreeVertices[0]))
				}
			}
		}
		
		reducedEdges.addAll(edges.collect { [edge: it, source: graph.getSource(it), dest: graph.getDest(it)] })
		println "RE: " + reducedEdges
		return createHelperGraph(reducedEdges)
	}
	
	def reduceExtraTerminals = { data ->
		def reducedData = new ArrayList(data)
		def terminalVerticesData = data.findAll { d ->
			d.dest.isTerminal()
		}
		
		if(terminalVerticesData.size() > 2) {
			['0', '1'].each { vtName ->
				def terminalToKeep = terminalVerticesData.find { tvd -> tvd.dest.name == vtName }
				if(terminalToKeep != null) {
					def terminalsToRedirect = terminalVerticesData.findAll { tvd ->
						tvd.dest.name == terminalToKeep.dest.name && tvd.dest != terminalToKeep.dest
					}
					terminalsToRedirect.each { tvd ->
						tvd.dest = terminalToKeep.dest
					}
				}
			}
		}
			
		return reducedData
	}
	
	def createHelperGraph = { edges ->
		Graph<MutableVertex, String> graph = new DirectedOrderedSparseMultigraph<MutableVertex, String>();
		edges.each {
			graph.addEdge(it.edge, it.source, it.dest)
		}
		
		return graph
	}
	
	def createHelperLayout = { graph ->
		Layout<MutableVertex, String> layout = null;
		int nLevels = app.controllers.FunctionDialog.view.cmbNVariables.selectedItem
		return  new BddTreeLayout<MutableVertex, String>(nLevels, graph, 50, 100);
	}
	
	def hasParallelsToReduce = { graph ->
		
		return graph.vertices.any { vertex ->
			if(!vertex.isTerminal()) {
				def outEdges = graph.getOutEdges(vertex)
				def subtreeVertices = outEdges.collect { graph.getDest(it)}
				
				return subtreeVertices[0] == subtreeVertices[1]
			} else {
				return false
			}
		}
	}
	
	def addStep = { vertices, edges, edgeObjects, graph, graphChange ->
		def copiedVertices = new ArrayList(vertices)
		def copiedEdgeObjects = new ArrayList(edgeObjects)
		copiedEdgeObjects += edges.collect { [edge: it, source: graph.getSource(it), dest: graph.getDest(it)] }
		model.steps << [vertices: copiedVertices, edges: copiedEdgeObjects, graphChange: graphChange]
	}
	
	def getSubtreeValues(vertex, graph) {
		def terminalSuccessors = findTerminalSuccessors(vertex, graph) 
		def terminalString = ""
		terminalSuccessors.each { v -> terminalString += v.name }
		return terminalString
	}
	
	def findTerminalSuccessors(vertex, graph) {
		if(vertex.isTerminal()) {
			return [vertex]
		}
		
		def terminalVertices = graph.getVertices().findAll { v -> v.isTerminal() }
		
		return terminalVertices.findAll { tv ->
//			println "Finding succesors: " + tv.dump()
			boolean isSuccessor = false
			def parentVertex = graph.getPredecessors(tv).iterator().next()
			
			while(!isSuccessor && (parentVertex instanceof MutableVertex)) {
				if(parentVertex == vertex) {
					isSuccessor = true
				}
				
				def predecessors = graph.getPredecessors(parentVertex)
				if(predecessors.size() > 0) {
					parentVertex = graph.getPredecessors(parentVertex).iterator().next()
				} else {
					parentVertex = "Mrk"
				}
			}
			
			return isSuccessor
		}
	}
	
	def animateForwardStep = {
		model.with {
			def currentEdges = new ArrayList(steps[currentStep].edges)
			def newEdges = new ArrayList(steps[currentStep + 1].edges)
			
			currentEdges.removeAll(newEdges)
			def expelledEdges = currentEdges
			
			fadeOut(expelledEdges)
			relocate(newEdges)
			
			def timer = new Timer()
			def task = timer.runAfter(1500) {
				redrawGraph(steps[currentStep].edges)
			}
		}
	} 
	
	def fadeOut(edges) {
		def vertices = [] as HashSet 
		edges.each { e ->
			vertices << e.source
			vertices << e.dest
		}
		
		vertices.each { vertex ->
			Timeline fadeTimeline = new Timeline(view.mainPanel);
			fadeTimeline.setDuration(700);
			PropertySetter<Point> propertySetter = new PropertySetter<Color>() {
						@Override
						public void set(Object obj, String fieldName, Color value) {
							vertex.color = value
							refresh();
						}
					};
			
			fadeTimeline.addPropertyToInterpolate(Timeline.<Color> property("value")
					.from(Color.black).to(model.graphPanel.background).setWith(propertySetter));
			fadeTimeline.play();
		}
	}
	
	def relocate(edges) {
		def graph = model.graph
		def layout = model.layout
		
		def newGraph = createHelperGraph(edges)
		def newLayout = createHelperLayout(newGraph)
		
		def vertices = newGraph.vertices;
		
		vertices.each { vertex ->
			Timeline movementTimeline = new Timeline(view.mainPanel);
			movementTimeline.setDuration(1000);
			PropertySetter<Point> propertySetter = new PropertySetter<Point>() {
						@Override
						public void set(Object obj, String fieldName, Point value) {
							model.layout.setLocation(vertex, value);
							refresh();
						}
					};
			
			def from = layout.transform(vertex)
			def to = newLayout.transform(vertex)
			
			def convert = { point2d ->
				new Point((int)point2d.x, (int)point2d.y)
			}
				
			movementTimeline.addPropertyToInterpolate(Timeline.<Point> property("value")
					.from(convert(from)).to(convert(to)).setWith(propertySetter));
			movementTimeline.play();
		}
	}
	
	def nextStep = {
		model.with {
			animateForwardStep()
			currentStep++ 
			//redrawGraph(steps[currentStep].edges)
//			println "---------------------"
//			println steps[currentStep].vertices.dump()
//			println steps[currentStep].edges.dump()
//			println "endendendendendendendendendend"
			updateStatus()
		}
		
	}
	
	def previousStep = {
		model.with {
			currentStep-- 
			redrawGraph(steps[currentStep].edges)
			updateStatus()
		}
	}
	
	def firstStep = {
		model.with {
			currentStep = 0
			redrawGraph(steps[currentStep].edges)
			updateStatus()
		}
	}
	
	def lastStep = {
		model.with {
			currentStep = nSteps - 2
			redrawGraph(steps[currentStep].edges)
			updateStatus()
		}
	}
	
	def updateStatus = {
		model.with {
			String status = ""
			(0..currentStep).each {  status += steps[it + 1].graphChange }
			view.txaStatus.text = status
		}
	}
	
	
}