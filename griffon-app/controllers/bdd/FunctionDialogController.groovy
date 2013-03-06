package bdd

import ca.odell.glazedlists.BasicEventList
import ca.odell.glazedlists.SortedList
import ca.odell.glazedlists.gui.AbstractTableComparatorChooser

class FunctionDialogController {
  // these will be injected by Griffon
  def model
  def view

  void mvcGroupInit(Map args) {
    println "Inicijalizacija"
    model.nVariablesList.addAll(1..4)
  }

  def onOk = {
    view.functionDialog.visible = false
    def nLevels = model.selected
    println "Broj nivoa: $nLevels"
    app.views.bdd.pnlReorder.changeNVarsTo(nLevels)
    app.controllers.bdd.redraw(model.tableModel, nLevels)
  }

}
