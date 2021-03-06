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
import graph.DyadicAutocorrelation
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
  boolean addingEnabled = true

  GraphZoomScrollPane graphScroll

  def v1

  void mvcGroupInit(Map args) {
    model.currentStep = -1
  }

  def openFunctionDialog = {
    app.views.FunctionDialog.functionDialog.visible = true
  }

  def refresh() {
    view.mainPanel.revalidate()
    view.mainPanel.repaint()
  }

  int findReorderedIndex(index, order) {
    def nLevels = order.size()
    String str = Integer.toString (index, 2).padLeft(nLevels, '0')
    def orderIndices = order.collect { Integer.parseInt (it[1]) }

    char[] finalStr = Integer.toString (index, 2).padLeft(nLevels, '0').getChars()
    (0..nLevels - 1).each { int i ->
      finalStr[orderIndices[i] - 1] = str[i]
    }
    return Integer.parseInt(finalStr as String, 2)
  }

  List createBddEdges(valueGetter) {
    def order = view.pnlReorder.order
    def nLevels = order.size()
    log.debug "ORDER KOJI KORISTIMO: $order"
    def vertices = []
    def fVertex = new MutableVertex("F", MutableVertex.ROOT)
    order.eachWithIndex { it, i ->
      (1..2**i).each {
        vertices << new MutableVertex(order[i])
      }
    }

    (0..2**nLevels - 1).each { it ->
      log.debug "debugging..."
      log.debug "$it"
      log.debug "$order"
      Integer index = findReorderedIndex(it, order)
      log.debug "Stampanje indeksa: " + index
      log.debug "Value returned: " + valueGetter(index)
      vertices << new MutableVertex(valueGetter(index), MutableVertex.TERMINAL)
    }

    //log.debug vertices.dump()

    def edges = []
    edges << [edge: new WeightedEdge(2), source: fVertex, dest: vertices[0] ]
    (0..2**nLevels - 2).each { i ->
      def startIndex = i * 2 + 1
      edges << [edge: new WeightedEdge(0), source: vertices[i], dest: vertices[startIndex]]
      edges << [edge: new WeightedEdge(1), source: vertices[i], dest: vertices[startIndex + 1]]
    }

    log.debug "Edges that we return: " + edges.dump()
    edges
  }

  def variablesReordered() {
    model.currentStep = -1
    model.graphDrawn = false
  }

  String closGetValueAt(index) {
    def functionModel = app.models.FunctionDialog.tableSortingModel
    functionModel[index].f ? "1" : "0"
  }

  def redrawReordered() {

    def edges = createBddEdges(this.&closGetValueAt)

    log.debug "Edges to redraw: "
    log.debug edges.dump()
    log.debug "Graph"
    redrawGraph(edges);

    model.functionDefined = true
    model.currentStep = -1
    model.autocorrelation = false
  }

  def redrawGraph(graphData) {
    Graph<MutableVertex, String> g = new DirectedOrderedSparseMultigraph<MutableVertex, String>();
    graphData.each {
      g.addEdge(it.edge, it.source, it.dest)
    }

    log.debug "Current step: " + model.currentStep
    DyadicAutocorrelation.printGraph(g)

    Layout<MutableVertex, String> layout = null;
    int nLevels = app.controllers.FunctionDialog.view.cmbNVariables.selectedItem
    layout = new BddTreeLayout<MutableVertex, String>(nLevels, g, 50, 100);

    //model.canvasWidth = view.mainScrollPane.width - 20
    //model.canvasHeight = view.mainScrollPane.height - 20
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

  List reduceGraph(graph) {
    List reducedEdges = reduceSubtrees(graph);
    reducedEdges = reduceParallelEdges(reducedEdges)
    reducedEdges = reduceExtraTerminals(reducedEdges)
    reducedEdges
  }

  def onReduceGraph = {
    model.steps = []
    model.autocorrelation = false
    def graph = model.graph
    def reducedEdges = reduceGraph(graph)

    redrawGraph(reducedEdges)
    def vertices = model.graph.vertices
    def edges = model.graph.edges
    addStep(vertices, edges, reducedEdges, model.graph, GraphChange.finished())
    model.nSteps = model.steps.size()
    model.currentStep = model.nSteps - 2
    printSteps()
    updateStatus()
  }

  List reduceSubtrees(graph) {
    log.debug "Reducing subtrees..."
    def reducedVertices = new ArrayList(graph.vertices)
    def reducedEdges = []
    def edges = new ArrayList(graph.edges)
    addStep(reducedVertices, edges, reducedEdges, graph, GraphChange.empty())


    def order = view.pnlReorder.order
    int nLevels = order.size()

    int i = 0
    def parsedVertices = []
    while(i < nLevels) {
      def vertexName = order[i]
      log.debug "Parsing vertex: $vertexName"
      def sameLevelVertices = reducedVertices.findAll { v ->
        v.name == vertexName && !parsedVertices.contains(v)
      }
      parsedVertices.addAll(sameLevelVertices)

      def outEdges = []
      sameLevelVertices.each { v -> outEdges.addAll(graph.getOutEdges(v)) }
      def subtreeVertices = outEdges.collect { e -> graph.getDest(e)}
      def values = subtreeVertices.collect { v -> getSubtreeValues(v, graph) }
      log.debug "Subtree values for current vertice: " 
      log.debug values.dump()

      // sequence, list of subtrees
      def sameValues = [:]

      // init with lists
      values.each { val ->
        sameValues[val] = []
      }

      values.eachWithIndex{ val, ind ->
        sameValues[val] << subtreeVertices[ind]
      }
      log.debug "SUBTREE MAP:" + sameValues
      sameValues.each { k, val ->
        log.debug "Parsing key: ${k}"
        if(val.size() > 1) {
          // subtree to redirect to
          def subtreeRoot = val[0]
          log.debug "Subtree root: ${subtreeRoot.dump()}"

          (1..val.size() - 1).each {
            def v = val[it]
            // remove subtree
            def subtree = BddHelper.getSubTree(graph, v)
            log.debug "Removing subtree:" 
            log.debug subtree.dump()

            reducedVertices -= subtree.getVertices()
            parsedVertices.addAll(subtree.getVertices())
            edges -= subtree.getEdges()

            log.debug "****REMOVED VERTICES: " + subtree.getVertices().dump()
            log.debug "****REMOVED EDGES: "
            subtree.getEdges().each { e -> 
              log.debug("" + subtree.getSource(e).dump() + "---$e--->" + subtree.getDest(e).dump())
            }


            //addStep(reducedVertices, edges, reducedEdges, graph, GraphChange.expelled(subtree))

            // redirect to other subtree
            def edgeToRedirect = graph.getInEdges(v).iterator().next()
            edges -= edgeToRedirect
            reducedEdges << [edge: edgeToRedirect, source: graph.getSource(edgeToRedirect), dest: subtreeRoot]
            log.debug "Edge for redirection: ${edgeToRedirect.dump()}"

            //							addStep(reducedVertices, edges, reducedEdges, graph,
            //								GraphChange.redirected(edgeToRedirect, v, subtreeRoot) )

            addStep(reducedVertices, edges, reducedEdges, graph, GraphChange.all(subtree.vertices, subtree.edges, edgeToRedirect, v, subtreeRoot))

            //log.debug "Reduced Vertices:" + reducedVertices
            log.debug " REDUCED EDGES:" + reducedEdges.dump()
          }
        }
      }
      i++;
    }
    reducedEdges.addAll(edges.collect { [edge: it, source: graph.getSource(it), dest: graph.getDest(it)] })
    return reducedEdges
  }

  List reduceParallelEdges(edges) {
    log.debug "Reducing parallel edges..."
    def graph = createHelperGraph(edges)

    while(hasParallelsToReduce(graph)) {
      graph = reduceSingleParallelEdge(graph)
    }
    def reducedEdges = []
    def finalEdges = new ArrayList(graph.edges)
    reducedEdges.addAll(finalEdges.collect { [edge: it, source: graph.getSource(it), dest: graph.getDest(it)] })

    return reducedEdges
  }

  Graph reduceSingleParallelEdge(graph) {
    def reducedVertices = new ArrayList(graph.vertices)
    def reducedEdges = []
    def edges = new ArrayList(graph.edges)
    //		log.debug "V: " + reducedVertices
    //		log.debug "E: " + edges
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
      log.debug ".............................VERTEX: " + vertex
      log.debug ".............................OE: " + outEdges
      log.debug ".............................SUBTREE VERTICES: " + subtreeVertices


      if(subtreeVertices[0] == subtreeVertices[1]) {
        reducedVertices -= vertex
        edges -= outEdges

        def edgesToRedirect = graph.getInEdges(vertex)
        edges -= edgesToRedirect
        edgesToRedirect.each { e ->
          def parentVertex = graph.getSource(e)
          if(subtreeVertices[0] == null) {
            log.debug "GRESKA!!!!"
            log.debug subtreeVertices.size()
            log.debug vertex
            log.debug outEdges
            log.debug edgeToRedirect
            log.debug subtreeVertices
            log.debug "_____________________________________________________________"
          }
          reducedEdges << [edge: e, source: parentVertex, dest: subtreeVertices[0] ]
          addStep(reducedVertices, edges, reducedEdges, graph, GraphChange.all([vertex], outEdges, e, vertex, subtreeVertices[0]))
        }
      }
    }

    reducedEdges.addAll(edges.collect { [edge: it, source: graph.getSource(it), dest: graph.getDest(it)] })
    log.debug "RE: " + reducedEdges
    return createHelperGraph(reducedEdges)
  }

  List reduceExtraTerminals(data) {
    log.debug "Reducing extra terminals..."
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

  Graph createHelperGraph(edges) {
    Graph<MutableVertex, String> graph = new DirectedOrderedSparseMultigraph<MutableVertex, String>();
    edges.each {
      graph.addEdge(it.edge, it.source, it.dest)
    }
    graph
  }

  Layout createHelperLayout(graph) {
    Layout<MutableVertex, String> layout = null;
    int nLevels = app.controllers.FunctionDialog.view.cmbNVariables.selectedItem
    return new BddTreeLayout<MutableVertex, String>(nLevels, graph, 50, 100);
  }

  boolean hasParallelsToReduce(graph) {

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

  void addStep(vertices, edges, edgeObjects, graph, graphChange) {
    edt {
      log.debug "Add step called - Adding enabled: $addingEnabled"
      if(addingEnabled) {
        def copiedVertices = new ArrayList(vertices)
        def copiedEdgeObjects = new ArrayList(edgeObjects)
        copiedEdgeObjects += edges.collect { [edge: it, source: graph.getSource(it), dest: graph.getDest(it)] }
        model.steps << [vertices: copiedVertices, edges: copiedEdgeObjects, graphChange: graphChange]
        log.debug "Added step: ${model.steps.size()}"
      }
      log.debug "Endof AddStep"
    }
  }

  void addWholeGraphStep(graph) {
    edt {
      addStep(graph.vertices, graph.edges, [], graph, GraphChange.empty())
    }
  }

  def getSubtreeValues(vertex, graph) {
    List terminalS = findTerminalSuccessors(vertex, graph)
    def terminalString = ""
    terminalS.each { v -> terminalString += v.name }
    return terminalString
  }

  List findTerminalSuccessors(vertex, graph) {
    if(vertex.isTerminal()) {
      return [vertex]
    }
    def terminalVertices = graph.getVertices().findAll { v -> v.isTerminal() }

    List terminalSuccessors = terminalVertices.findAll { tv ->
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

    return terminalSuccessors
  }

  def animateForwardStep() {
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
      //			log.debug "---------------------"
      //			log.debug steps[currentStep].vertices.dump()
      //			log.debug steps[currentStep].edges.dump()
      //			log.debug "endendendendendendendendendend"
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
      if(!autocorrelation) {
        (0..currentStep).each {  if(it != nSteps - 1) status += steps[it + 1].graphChange }
      }
      view.txaStatus.text = status
    }
  }

  def printSteps() {
    log.debug "Trace steps:"
    model.steps.eachWithIndex { s, i ->
      log.debug "$i"
      log.debug s.edges.dump()
    }
    log.debug "End of steps printing."
  }

  def calculateAutocorrelation = {
    edt {
      model.autocorrelation = true
      log.debug "Test integration with Dyadic autocorrelation class"

      def da = new DyadicAutocorrelation()
      //da.testDyadic()
      //da.testSpectra()
      int nLevels = view.pnlReorder.order.size()
      model.steps = []

      log.debug "`````````````````````Starting calculation of autocorrelation..."
      def edges = createBddEdges(this.&closGetValueAt)
      def helperGraph = createHelperGraph(edges)
      addWholeGraphStep(helperGraph)
      DyadicAutocorrelation.printGraph(helperGraph)
      log.debug "START***************************************************************"

      log.debug "Reduce graph"
      addingEnabled = false
      def reducedGraph = createHelperGraph(reduceGraph(helperGraph))
      addingEnabled = true
      addWholeGraphStep(reducedGraph)
      DyadicAutocorrelation.printGraph(reducedGraph)
      log.debug "0***************************************************************"

      log.debug "Add crosspoints"
      def graphWithCp = da.addCrosspoints(reducedGraph, nLevels)
      edges = graphWithCp.edges
      addWholeGraphStep(graphWithCp)
      DyadicAutocorrelation.printGraph(graphWithCp)
      log.debug "1***************************************************************"
      // Temp drawing to check crosspoints
      //    def copiedEdgeObjects = edges.collect { [edge: it, source: graphWithCp.getSource(it), dest: graphWithCp.getDest(it)] }
      //    redrawGraph(copiedEdgeObjects)

      log.debug "Calculate spectra"
      def spectra = da.calculateSpectra(graphWithCp)

      log.debug "Square spectra"
      spectra = spectra.collect { it * it }

      // Create graph. Draw it only to get helper graph (in model) required for the next step
      edges = createBddEdges({ spectra[it] as String})
      helperGraph = createHelperGraph(edges)
      addWholeGraphStep(helperGraph)
      DyadicAutocorrelation.printGraph(helperGraph)
      log.debug "2***************************************************************"


      // reduce
      addingEnabled = false
      reducedGraph = createHelperGraph(reduceGraph(helperGraph))
      addingEnabled = true
      addWholeGraphStep(reducedGraph)
      DyadicAutocorrelation.printGraph(reducedGraph)
      log.debug "3***************************************************************"


      // add crosspoints
      graphWithCp = da.addCrosspoints(reducedGraph, nLevels)
      addWholeGraphStep(graphWithCp)
      DyadicAutocorrelation.printGraph(graphWithCp)
      log.debug "4***************************************************************"

      // calculate spectra
      spectra = da.calculateSpectra(graphWithCp)
      def autoCorrelation = spectra.collect { (1/2**nLevels * it) as Integer }
      edges = createBddEdges({ autoCorrelation[it] as String})
      helperGraph = createHelperGraph(edges)
      addWholeGraphStep(helperGraph)
      DyadicAutocorrelation.printGraph(helperGraph)
      log.debug "5***************************************************************"
      log.debug "$autoCorrelation"


      // Add final step
      addStep(helperGraph.vertices, helperGraph.edges, [], helperGraph, GraphChange.finished())

      model.nSteps = model.steps.size()
      model.currentStep = model.nSteps - 2

      redrawGraph(model.steps[model.currentStep].edges)
      updateStatus()

      log.debug "${model.nSteps}"
      log.debug "${model.currentStep}"
      log.debug "auto correlation done!"
    }
  }
}
