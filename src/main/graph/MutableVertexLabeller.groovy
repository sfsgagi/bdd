package graph

import org.apache.commons.collections15.Transformer;

class MutableVertexLabeller<V> implements Transformer<V,String> {

    public String transform(V v) {
		return "$v.name"
	}
}
