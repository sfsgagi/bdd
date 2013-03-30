package graph

import org.apache.commons.collections15.Transformer;

class MutableVertexLabeller<V> implements Transformer<V,String> {

  public String transform(V v) {
    if("$v.name".contains("\u00a9"))
      return "\u00a9"
    else 
      return "$v.name"
  }
}
