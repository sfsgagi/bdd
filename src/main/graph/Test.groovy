package graph

import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph 
import edu.uci.ics.jung.graph.Graph;


//int i = 4
//def model = (0..(2**i - 1)).collect {
//	String strRow = Integer.toString (it, 2).padLeft(i, '0')
//	def row = [:]
//	(1..i).each {
//		row['x' + it] = strRow[it - 1] 
//	}
//	row
//}
//println model


//def pera = [1, 2, 3, 5]
//def ninja = pera.clone()
//
//println pera
//println ninja
//
//pera.clear()
//
//println pera
//println ninja





/** TEST za konverziju
 * 
 
def nLevels = 4
def order = ["x2", "x4", "x3", "x1"]

def convert = { index ->
	String str = Integer.toString (index, 2).padLeft(nLevels, '0')
	def orderIndices = order.collect { Integer.parseInt (it[1]) }
	
	char[] finalStr = Integer.toString (index, 2).padLeft(nLevels, '0').getChars()
	(0..nLevels - 1).each { int i ->
		finalStr[orderIndices[i] - 1] = str[i]   
	}
	
	return Integer.parseInt(finalStr as String, 2)
}


//x2x4x3x1 0101 (5) je u stvari x1x2x3x4 1001 (9)
println convert(5)
/** end */


/** Test za redukciju extra terminala
 */

def v1 = new MutableVertex('1');
def v2 = new MutableVertex('2');
def v3 = new MutableVertex('3');
def t1 = new MutableVertex('1', MutableVertex.TERMINAL)
def t2 = new MutableVertex('0', MutableVertex.TERMINAL)
def t3 = new MutableVertex('1', MutableVertex.TERMINAL)
def t4 = new MutableVertex('0', MutableVertex.TERMINAL)

def graphData = []

graphData << [edge: new WeightedEdge(0), source: v1, dest: t1]
graphData << [edge: new WeightedEdge(1), source: v1, dest: v2]
graphData << [edge: new WeightedEdge(0), source: v2, dest: t2]
graphData << [edge: new WeightedEdge(1), source: v2, dest: v3]
graphData << [edge: new WeightedEdge(0), source: v3, dest: t3]
graphData << [edge: new WeightedEdge(0), source: v3, dest: t4]

def createHelperGraph = { edges ->
	Graph<MutableVertex, String> graph = new DirectedOrderedSparseMultigraph<MutableVertex, String>();
	edges.each {
		graph.addEdge(it.edge, it.source, it.dest)
	}
	
	return graph
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


def printGraph = { Graph graph ->
	println "Edges: "
	graph.edges.each { e ->
		println "" + graph.getSource(e).vertexId + "->" + graph.getDest(e).vertexId
	}
	println "----------------"
}
def testGraph = createHelperGraph(graphData) 
printGraph(testGraph)
def reducedGraphData = reduceExtraTerminals(graphData)
printGraph(createHelperGraph(reducedGraphData))

/** end */

