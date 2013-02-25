package graph

class WeightedEdge {
	static int counter = 1
	int weight
	int id
	
	WeightedEdge(int w, Integer number) {
		this.id = number
		weight = w
	}
	
	WeightedEdge(int w) {
		this.id = counter++
		weight = w
	}
	
	String toString() {
		if(weight > 1) {
			return ""
		}
		else {
			return "$weight"
		}
	}
}
