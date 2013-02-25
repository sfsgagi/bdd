package bdd

import edu.uci.ics.jung.algorithms.layout.Layout 
import edu.uci.ics.jung.graph.Graph;
import graph.GraphPanel 
import graph.MutableVertex 
import graph.WeightedEdge 
import groovy.beans.Bindable;

class BddModel {
	int canvasWidth
	int canvasHeight
	Graph<MutableVertex, WeightedEdge> graph
	Layout<MutableVertex, String> layout
	GraphPanel graphPanel
	
	def steps
	@Bindable int currentStep
	@Bindable int nSteps
	@Bindable boolean functionDefined
	@Bindable boolean graphDrawn
}