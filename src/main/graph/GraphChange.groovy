package graph

class GraphChange {
	def expelledEdges
	def expelledVertices
	def edgeRedirectedData
		
	static empty() {
		return [expelledEdges: [], expelledVertices: [],
			edgeRedirectedData: [:]] as GraphChange
	}
	
	static expelled(subgraph) {
		return [expelledEdges: subgraph.edges, expelledVertices: subgraph.vertices, 
			edgeRedirectedData: [:]] as GraphChange
	}
	
	static redirected(edge, from , to) {
		return [expelledEdges: [], expelledVertices: [], 
			edgeRedirectedData: [edge: edge, from: from, to: to]] as GraphChange
	}
	
	static all(expelledVertices, expelledEdges, redirectedEdge, redirectedFrom, redirectedTo) {
		return [expelledEdges: expelledEdges, expelledVertices: expelledVertices,
			edgeRedirectedData: [edge: redirectedEdge, from: redirectedFrom, to: redirectedTo]] as GraphChange
	}
	
	static finished() {
		return [expelledEdges: ["FINAL"], expelledVertices: ["FINAL"], 
			edgeRedirectedData: [:]] as GraphChange
	}
	
	String toString() {
return """\
Expelled vertices: $expelledVertices
Expelled edges: $expelledEdges
Edge $edgeRedirectedData.edge redirected from $edgeRedirectedData.from to $edgeRedirectedData.to
"""
	}
}
