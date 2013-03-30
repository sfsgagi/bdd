package graph


import java.awt.Cursor;
import bdd.BddController
import groovy.swing.SwingBuilder;

import java.awt.BorderLayout
import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable
import java.awt.dnd.DnDConstants
import java.awt.dnd.DragGestureListener
import java.awt.dnd.DragSource
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetAdapter
import java.awt.dnd.DropTargetDragEvent
import java.awt.dnd.DropTargetDropEvent
import java.awt.dnd.DropTargetEvent
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel
import net.miginfocom.swing.MigLayout

import org.slf4j.Logger
import org.slf4j.LoggerFactory


class ReorderPanel extends JPanel {
  static final Logger log = LoggerFactory.getLogger(ReorderPanel.class)

  List order
  SwingBuilder swing
  BddController controller

  ReorderPanel(nVars, bddController) {
    order = (1..nVars).collect { "x" + it }
    swing = new SwingBuilder()
    this.controller = bddController

    init();
  }

  def init() {
    def content = swing.panel(layout: new MigLayout("insets 0")) {
      order.eachWithIndex { var, i ->
        panel(id: "pnl$i", constraints: "w 30", cursor: new Cursor(Cursor.MOVE_CURSOR),
          border: BorderFactory.createEmptyBorder(0, 0, 10, 0)) {  label(id: "lbl$i", text: var) }
        panel(constraints: "width 2px!, height 15px!", background: Color.gray)
      }
    }


    setLayout(new BorderLayout())
    add(content, BorderLayout.CENTER)
    initDnD()
  }

  def initDnD() {
    order.eachWithIndex { var, i ->
      def ddPanel = swing."pnl$i"
      new MyDropTargetListener(ddPanel, i, this);

      DragSource ds = new DragSource();
      ds.createDefaultDragGestureRecognizer(ddPanel,
          DnDConstants.ACTION_MOVE,
          { dge ->
            dge.startDrag(null, new StringSelection(i.toString()));
          } as DragGestureListener);

    }
  }

  // x1, x2, x3, etc. (not reordered)
  boolean isOrderNormal() {
    order.every { el ->
      "x${order.findIndexOf { it == el } + 1}" == el
    }
  }

  def refresh(dragedInd, dropedInd) {
//    int dropedInd = order.indexOf(dropedVar)
//    int dragedInd = order.indexOf(dragedVar)
    def dragedVar = order[dragedInd]
    def dropedVar = order[dropedInd]
    log.debug "ORDER PRE: $order"
    log.debug "Draged var: $dragedVar"
    log.debug "Droped var: $dropedVar"
    log.debug "Draged ind: $dragedInd"
    log.debug "Droped ind: $dropedInd"

    order.set(dropedInd, dragedVar)
    order.set(dragedInd, dropedVar)

    log.debug "ORDER AFTER: $order"


    order.eachWithIndex { var, i ->
      swing."lbl$i".text = var
    }

    revalidate()
    controller.variablesReordered()
  }

  def changeNVarsTo(nVars) {
    order = (1..nVars).collect { "x" + it }
    removeAll()
    init()
    revalidate()
  }
}

class MyDropTargetListener extends DropTargetAdapter {

  private DropTarget dropTarget;
  private JPanel panel;
  private int index;
  private ReorderPanel reorderPanel;

  public MyDropTargetListener(JPanel panel, int index, reorderPanel) {
    this.index = index
    this.panel = panel;
    this.reorderPanel = reorderPanel

    dropTarget = new DropTarget(panel, DnDConstants.ACTION_MOVE,
        this, true, null);
  }


  public void drop(DropTargetDropEvent event) {
    try {

      Transferable tr = event.getTransferable();
      int dragedInd = Integer.parseInt(tr.getTransferData(DataFlavor.stringFlavor))

      event.acceptDrop(DnDConstants.ACTION_MOVE);
      event.dropComplete(true);
      panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0))
      reorderPanel.refresh(dragedInd, index)

    } catch (Exception e) {
      e.printStackTrace();
      event.rejectDrop();
    }
  }

  void dragEnter(DropTargetDragEvent arg0) {
    panel.setBorder(BorderFactory.createMatteBorder(0, 0, 10, 0, Color.black))
  }

  void dragExit(DropTargetEvent arg0) {
    panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0))
  }
}

