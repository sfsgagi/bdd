package graph;

import java.util.Collection;

import edu.uci.ics.jung.graph.AbstractGraph;

public class BddHelper {
  @SuppressWarnings("unchecked")
  public static <V,E> AbstractGraph<V,E> getSubTree(AbstractGraph<V,E> forest, V root) throws InstantiationException, IllegalAccessException 
  {
    AbstractGraph<V,E> subforest = forest.getClass().newInstance();
    subforest.addVertex(root);
    growSubTree(forest, subforest, root);

    return subforest;
  }

  /**
   * Populates <code>subtree</code> with the subtree of <code>tree</code> 
   * which is rooted at <code>root</code>.
   * @param <V> the vertex type
   * @param <E> the edge type
   * @param tree the tree whose subtree is to be extracted
   * @param subTree the tree instance which is to be populated with the subtree of <code>tree</code>
   * @param root the root of the subtree to be extracted
   */
  public static <V,E> void growSubTree(AbstractGraph<V,E> tree, AbstractGraph<V,E> subTree, V root) {
    if(tree.getSuccessorCount(root) > 0) {
      Collection<E> edges = tree.getOutEdges(root);
      for(E e : edges) {
        subTree.addEdge(e, tree.getEndpoints(e));
      }
      Collection<V> kids = tree.getSuccessors(root);
      for(V kid : kids) {
        growSubTree(tree, subTree, kid);
      }
    }
  }
}
