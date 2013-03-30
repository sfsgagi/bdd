package graph;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LazyMap;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.AbstractGraph;
import edu.uci.ics.jung.graph.Graph;

public class BddTreeLayout<E> implements Layout<MutableVertex,E> {

  protected Dimension size = new Dimension(600,600);
  protected AbstractGraph<MutableVertex,E> graph;
  protected Map<MutableVertex,Integer> basePositions = new HashMap<MutableVertex,Integer>();

  protected Map<MutableVertex, Point2D> locations =
    LazyMap.decorate(new HashMap<MutableVertex, Point2D>(),
        new Transformer<MutableVertex,Point2D>() {
          public Point2D transform(MutableVertex arg0) {
            return new Point2D.Double();
          }});

  protected transient Set<MutableVertex> alreadyDone = new HashSet<MutableVertex>();

  /**
   * The default horizontal vertex spacing.  Initialized to 50.
   */
  public static int DEFAULT_DISTX = 50;

  /**
   * The default vertical vertex spacing.  Initialized to 50.
   */
  public static int DEFAULT_DISTY = 50;

  /**
   * The horizontal vertex spacing.  Defaults to {@code DEFAULT_XDIST}.
   */
  protected int distX = 50;

  /**
   * The vertical vertex spacing.  Defaults to {@code DEFAULT_YDIST}.
   */
  protected int distY = 50;

  protected int nLevels = 0;

  protected transient Point m_currentPoint = new Point();

  /**
   * Creates an instance for the specified graph with default X and Y distances.
   */
  public BddTreeLayout(AbstractGraph<MutableVertex,E> g) {
    this(g, DEFAULT_DISTX, DEFAULT_DISTY);
  }
  public BddTreeLayout(int nLevels, AbstractGraph<MutableVertex,E> g, int distx, int disty) {
    this.nLevels = nLevels + 2;
    if (g == null)
      throw new IllegalArgumentException("Graph must be non-null");
    if (distx < 1 || disty < 1)
      throw new IllegalArgumentException("X and Y distances must each be positive");
    this.graph = g;
    this.distX = distx;
    this.distY = disty;
    buildTree();
  }

  /**
   * Creates an instance for the specified graph and X distance with
   * default Y distance.
   */
  public BddTreeLayout(AbstractGraph<MutableVertex,E> g, int distx) {
    this(g, distx, DEFAULT_DISTY);
  }

  /**
   * Creates an instance for the specified graph, X distance, and Y distance.
   */
  public BddTreeLayout(AbstractGraph<MutableVertex,E> g, int distx, int disty) {
    if (g == null)
      throw new IllegalArgumentException("Graph must be non-null");
    if (distx < 1 || disty < 1)
      throw new IllegalArgumentException("X and Y distances must each be positive");
    this.graph = g;
    this.distX = distx;
    this.distY = disty;
    buildTree();
  }

  private Collection<MutableVertex> getRoots() {
    Collection<MutableVertex> roots = new ArrayList<MutableVertex>();
    List<MutableVertex> sorted = new ArrayList<MutableVertex>(graph.getVertices());
    Collections.sort(sorted);
    MutableVertex root = sorted.iterator().next();
    roots.add(root);
    return roots;
  }

  protected void buildTree() {
    this.m_currentPoint = new Point(0, 20);
    Collection<MutableVertex> roots = getRoots();
    if (roots.size() > 0 && graph != null) {
      calculateDimensionX(roots);
      for(MutableVertex v : roots) {
        calculateDimensionX(v);
        m_currentPoint.x += this.basePositions.get(v)/2 + this.distX;
        buildTree(v, this.m_currentPoint.x);
      }
    }
    int width = 0;
    for(MutableVertex v : roots) {
      width += basePositions.get(v);
    }
  }

  protected void buildTree(MutableVertex v, int x) {

    if (!alreadyDone.contains(v)) {
      alreadyDone.add(v);


      //go one level further down



      this.m_currentPoint.y += this.distY;
      this.m_currentPoint.x = x;

      this.setCurrentPositionFor(v);

      int sizeXofCurrent = basePositions.get(v);

      int lastX = x - sizeXofCurrent / 2;

      int sizeXofChild;
      int startXofChild;

      for (MutableVertex element : graph.getSuccessors(v)) {
        sizeXofChild = this.basePositions.get(element);
        startXofChild = lastX + sizeXofChild / 2;
        buildTree(element, startXofChild);
        lastX = lastX + sizeXofChild + distX;
      }
      this.m_currentPoint.y -= this.distY;
    }
  }

  private int calculateDimensionX(MutableVertex v) {

    int size = 0;
    int childrenNum = graph.getSuccessors(v).size();

    if (childrenNum != 0) {
      for (MutableVertex element : graph.getSuccessors(v)) {
        size += calculateDimensionX(element) + distX;
      }
    }
    size = Math.max(0, size - distX);
    basePositions.put(v, size);

    return size;
  }

  private int calculateDimensionX(Collection<MutableVertex> roots) {

    int size = 0;
    for(MutableVertex v : roots) {
      int childrenNum = graph.getSuccessors(v).size();

      if (childrenNum != 0) {
        for (MutableVertex element : graph.getSuccessors(v)) {
          size += calculateDimensionX(element) + distX;
        }
      }
      size = Math.max(0, size - distX);
      basePositions.put(v, size);
    }

    return size;
  }

  /**
   * This method is not supported by this class.  The size of the layout
   * is determined by the topology of the tree, and by the horizontal
   * and vertical spacing (optionally set by the constructor).
   */
  public void setSize(Dimension size) {
    throw new UnsupportedOperationException("Size of TreeLayout is set" +
        " by vertex spacing in constructor");
  }

  protected void setCurrentPositionFor(MutableVertex vertex) {
    int x = m_currentPoint.x;
    int y = m_currentPoint.y;
    if(x < 0) size.width -= x;

    if(x > size.width-distX)
      size.width = x + distX;

    if(y < 0) size.height -= y;
    if(y > size.height-distY)
      size.height = y + distY;

    int posY = m_currentPoint.y;
    if(vertex.isTerminal()) {
      posY = 20 + nLevels * distY;
    }
    Point loc = new Point(m_currentPoint.x, posY);

    locations.get(vertex).setLocation(loc);

  }

  public Graph<MutableVertex,E> getGraph() {
    return graph;
  }

  public Dimension getSize() {
    return size;
  }

  public void initialize() {

  }

  public boolean isLocked(MutableVertex v) {
    return false;
  }

  public void lock(MutableVertex v, boolean state) {
  }

  public void reset() {
  }

  public void setGraph(Graph<MutableVertex,E> graph) {
    if(graph instanceof AbstractGraph) {
      this.graph = (AbstractGraph<MutableVertex,E>)graph;
      buildTree();
    } else {
      throw new IllegalArgumentException("graph must be an AbstractGraph");
    }
  }

  public void setInitializer(Transformer<MutableVertex, Point2D> initializer) {
  }

  /**
   * Returns the center of this layout's area.
   */
  public Point2D getCenter() {
    return new Point2D.Double(size.getWidth()/2,size.getHeight()/2);
  }

  public void setLocation(MutableVertex v, Point2D location) {
    locations.get(v).setLocation(location);
  }

  public Point2D transform(MutableVertex v) {
    return locations.get(v);
  }
}
