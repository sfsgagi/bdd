package bdd

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.SortedList
import ca.odell.glazedlists.gui.AbstractTableComparatorChooser

class FunctionDialogController {
  // these will be injected by Griffon
  def model
  def view

  void mvcGroupInit(Map args) {
    println "Function dialog initialization"
    model.nVariablesList.addAll(1..4)
  }

  def onOk = {
    view.functionDialog.visible = false
    def nVars = model.nVars
    println "Levels: $nVars"
    app.views.bdd.pnlReorder.changeNVarsTo(nVars)
    app.controllers.bdd.redrawReordered()
  }

}
