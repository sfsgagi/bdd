package test;
import java.awt.*;
import javax.swing.*;
import java.util.Vector;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;

public class QuadCurveExample extends JApplet {
 Canvas1 canvas;
public static void main(String[] args){
      JFrame frame = new JFrame("QuadCurve Example");
      QuadCurveExample curve = new QuadCurveExample();
      curve.init();
      frame.getContentPane().add(curve);
      frame.setSize(500,250);
      frame.setVisible(true);
  }
  public void init() {
    Container container = getContentPane();
    canvas = new Canvas1();
    container.add(canvas);
  }
  class Canvas1 extends Canvas {
    Vector vector;
    QuadCurve2D quadCurve = null;
    Rectangle2D rec = null;

    public Canvas1() {
      setBackground(Color.white);
      setSize(500, 250);

      vector = new Vector();
      vector.addElement(new QuadCurve2D.Float(30, 30, 90, 
         170, 130,30));
      vector.addElement(new QuadCurve2D.Float(130, 110, 170, 
         50, 210,190));
      
    }
     public void paint(Graphics g) {
      Graphics2D g2 = (Graphics2D) g;
     for (int k = 0; k < vector.size(); k++) {
        g2.draw((QuadCurve2D) vector.elementAt(k));
      }
}
  }
}