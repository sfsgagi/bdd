package graph
import java.awt.Color 

public class MutableVertex implements Comparable<MutableVertex> {
	public final static String NODE = "NODE";
	public final static String ROOT = "ROOT";
	public final static String TERMINAL = "TERMINAL";
	public final static String CP = "CP";
	static counter = 1;
	Integer vertexId
	String name = ""
	String type
	Color color
	
	MutableVertex(String n) {
		vertexId = counter++
		this.name = n
		type = NODE		
	}
	MutableVertex(String n, String type) {
		this(n)
		this.type = type
	}

  public int intValue() {
    return isTerminal() ? Integer.parseInt(name) : 0
  }
	
	public String toString() {
		return "id: $vertexId [$name]"
	}
	
	boolean isTerminal() {
		return type == TERMINAL
	}
	
	boolean isRoot() {
		return type == ROOT
	}

  boolean isCrosspoint() {
    return type == CP
  }

  boolean isNode() {
    return type == NODE
  }
	
	Color findColor() {
		if(color != null) {
			return color
		} else if(isRoot()) {
			return Color.black;
		} else if(isTerminal()) {
			return new Color(0xb4d8e7)
		} else {
			return Color.yellow;
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + vertexId;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MutableVertex other = (MutableVertex) obj;
//		if(isTerminal() && name == other.name) {
//			return true
//		} else 
		if (vertexId != other.vertexId)
			return false;
		return true;
	}
	
	@Override
	public int compareTo(MutableVertex o) {
		return this.vertexId.compareTo(o.vertexId);
	}
}
