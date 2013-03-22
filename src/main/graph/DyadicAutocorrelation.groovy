package graph

import static Matrix.*
import static Plot.*
// graph imports
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


class DyadicAutocorrelation {

  def bruteForceAutoCorrelation(double [] x, double [] ac) {
    Arrays.fill(ac, 0);
    int n = x.length;
    for (int j = 0; j < n; j++) {
      for (int i = 0; i < n; i++) {
        ac[j] += x[i] * x[(n + i - j) % n];
      }
    }
  }

  // Regular dyadic autocorrelation calculation
  def dyadicAutoCorrelation(f) {
    int n = f.size()
    int[] ac = new int[n]
    f.eachWithIndex { fi, i ->
      (0..n - 1).each { tau ->
        //println "$i: tau = $tau, i exor tau = ${i ^ tau} fi = $fi, fitau = ${f[i ^ tau]} "
        ac[tau] += fi * f[i ^ tau]
        //println ac
      }
    }
    ac
  }


  // Walsh matrix
  def W = new Matrix([[1, 1], [1, -1]])

  // Calculate dyadic autocorrelation using Wiener–Khinchin theorem
  def wkDyadicAutoCorrelation(f) {
    def nVars = log2(f.size())
    def Wn = chronecker(W, nVars)
    def Sf = Wn * transpose(new Matrix(f))
    return (Wn * Sf.raise(2)) * (1/2**nVars)
  }

  def log2(x) {
    Math.log(x)/Math.log(2)
  }


  def fastWalshDyadicAutoCorrelation(f){
    // ovo nece treba
  }

  def testDyadic() {

    def f = [0, 0, 1, 0, 1, 1, 1, 1]

    //println dyadicAutoCorrelation(f)
    println W
    /*println W.chronecker(W).chronecker(W)
    println "krace"
    println chronecker(W, 2)*/
    println dyadicAutoCorrelation(f)
    println "____________________________"
    println wkDyadicAutoCorrelation(f)
  }

  def f = new MutableVertex('f', MutableVertex.ROOT)
  def v1 = new MutableVertex('x1');
  def v2 = new MutableVertex('x2');
  def v3 = new MutableVertex('x3');
  def t0 = new MutableVertex('0', MutableVertex.TERMINAL)
  def t1 = new MutableVertex('1', MutableVertex.TERMINAL)
  def t2 = new MutableVertex('2', MutableVertex.TERMINAL)
  def t3 = new MutableVertex('3', MutableVertex.TERMINAL)


  // Example from Spectral Logic and Its Applications for the Design of Digital Devices, Karpovsky, Stankovic, Astola,
  // (page 145, Fig. 3.5.2) (Reduced graph)
  def graphData = [
  [edge: new StringEdge(""), source: f, dest: v1],
  [edge: new StringEdge("z0'"), source: v1, dest: v2],
  [edge: new StringEdge("z0"), source: v1, dest: t3],
  [edge: new StringEdge("z1'"), source: v2, dest: t0],
  [edge: new StringEdge("z1"), source: v2, dest: v3],
  [edge: new StringEdge("z2'"), source: v3, dest: t1],
  [edge: new StringEdge("z2"), source: v3, dest: t2]
  ]

  // Simple example to test crosspoints from root to terminal
  def graphData1 = [[edge: new StringEdge(""), source: f, dest: t3]]

  // Example from: Analysis of Decision Diagram based Methods for the Calculation of the Dyadic Autocorrelation, Radmanovic et al.
  // Figure 4.1 (Reduced graph)
  def graphData2 = [
  [edge: new StringEdge(""), source: f, dest: v1],
  [edge: new StringEdge("0"), source: v1, dest: v2],
  [edge: new StringEdge("1"), source: v1, dest: t1],
  [edge: new StringEdge("0"), source: v2, dest: t0],
  [edge: new StringEdge("1"), source: v2, dest: v3],
  [edge: new StringEdge("0"), source: v3, dest: t1],
  [edge: new StringEdge("1"), source: v3, dest: t0]
  ]

  def createHelperGraph = { edges ->
    Graph<MutableVertex, String> graph = new DirectedOrderedSparseMultigraph<MutableVertex, String>();
    edges.each {
      println "Adding edge: " + graph.addEdge(it.edge, it.source, it.dest)
    }

    return graph
  }

  def printGraph = { Graph graph ->
    println "Edges: "
    graph.edges.each { e ->
      println "" + graph.getSource(e).name + "---$e--->" + graph.getDest(e).name
    }
    println "----------------"
  }

  def addCrosspoints(graph) {

    def order = (1..3).collect { "x" + it }
    def currentLevel = 0
    def root = graph.vertices.find { v -> v.isRoot() }

    def currentVertices = [root]

    while(!currentVertices.empty && currentLevel < order.size()) {
      def currentEndpoint = order[currentLevel]
      def edges = []
      currentVertices.each { v ->
        graph.getOutEdges(v).each { edges << it }
      }

      println "Parsing edges: "
      edges.each { e ->
        println "" + graph.getSource(e).name + "---$e--->" + graph.getDest(e).name
      }
      currentVertices = []
      for(def e : edges) {
        def source = graph.getSource(e)
        def dest = graph.getDest(e)
        if(!source.isTerminal()) {
          if(!dest.name.equals(currentEndpoint)) {
            // insert crosspoint
            MutableVertex cp = new MutableVertex("\u00a9$currentEndpoint", MutableVertex.CP)
            graph.addEdge(new StringEdge(e), source, cp)
            graph.addEdge(new StringEdge("(-)"), cp, dest)
            graph.removeEdge(e)
            currentVertices << cp
          } else {
            currentVertices << dest
          }
        }
      }
      currentLevel++
      println "Current vertices: " + currentVertices
    }

    graph
  }

  def getParents(graph, vertices) {
    def parents = new HashSet()
    // start with lowest level
    vertices.each { t ->
      graph.getInEdges(t).each { parents << graph.getSource(it) }
    }
    parents
  }

  def calculateSpectra(graph) {
    def terminals = graph.vertices.findAll { v -> v.isTerminal() }
    def currentParents = getParents(graph, terminals)
    def mapVerticeToVector = [:]
    def spectra
    println currentParents
    while(currentParents.size() > 0 && !currentParents.first().isRoot()) {
      currentParents.each { v ->
        def outEdges = graph.getOutEdges(v)
        assert outEdges.size() == 1 || outEdges.size() == 2
        def dest1, dest2

        if(outEdges.size() == 1) {
          dest1 = dest2 = graph.getDest(outEdges.first())
        } else {
          def e1 = outEdges.find { e -> e.text == "0" }
          def e2 = outEdges.find { e -> e.text == "1" }
          dest1 = graph.getDest(e1)
          dest2 = graph.getDest(e2)
        }
        if(mapVerticeToVector.containsKey(dest1) && mapVerticeToVector.containsKey(dest2)) {
          def vec1 = mapVerticeToVector[dest1]
          def vec2 = mapVerticeToVector[dest2] as Queue

          println vec1
          println vec2

          def firstPart = vec1.collect { it + vec2.poll() }
          vec2 = mapVerticeToVector[dest2] as Queue
          def secondPart = vec1.collect { it - vec2.poll() }
          def finalVec = [firstPart, secondPart].flatten()
          println "Final vec: " + finalVec
          mapVerticeToVector[v] = finalVec
          spectra = finalVec
        } else {
          assert dest1.isTerminal() && dest2.isTerminal()
          println "Destinations: "
          println "$dest1, $dest2"
          int v1 = dest1.intValue()
          int v2 = dest2.intValue()

          mapVerticeToVector[v] = [ v1 + v2, v1 - v2 ]
          println "Vector for $v: [$v1, $v2] "
          println mapVerticeToVector[v]
          spectra = mapVerticeToVector[v]
        }
      }


      println "***********************"
      currentParents = getParents(graph, currentParents)
      println currentParents
    }

    spectra
  }

  def ddAutoCorrelation(reducedGraph) {
    nLevels = 3

    def spectra = calculateSpectra(reducedGraph)
    spectra = spectra.collect { it * it }

    // create graph
    // TODO

    // reduce
    // TODO

    // calculate spectra
    // TODO

    def autoCorrelation = spectra.collect { 1/2**nLevels * it }
    autocorrelation

  }

  def testSpectra() {
    def testGraph = createHelperGraph(graphData2)
    printGraph(testGraph)

    def newGraph = addCrosspoints(testGraph)
    printGraph(newGraph)

    println "Starting calculation of spectra of DD..."
    println calculateSpectra(newGraph)
  }
}

