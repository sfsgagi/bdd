package bdd

import javax.swing.JPanel
import net.miginfocom.swing.MigLayout
import ca.odell.glazedlists.swing.TableComparatorChooser
import ca.odell.glazedlists.gui.AbstractTableComparatorChooser

dialog(owner: app.views.bdd, title: 'Enter function',
    id: "functionDialog",
    size: [320,480],
    locationByPlatform:true,
    locationRelativeTo: null,
    visible: false,
    modal: true,
    //resizable: false,
    iconImage: imageIcon('/griffon-icon-48x48.png').image,
    iconImages: [
    imageIcon('/griffon-icon-48x48.png').image,
    imageIcon('/griffon-icon-32x32.png').image,
    imageIcon('/griffon-icon-16x16.png').image]) {

  migLayout()
  label('Select the number of variables:')
  comboBox(id: 'cmbNVariables', model: eventComboBoxModel(source: model.nVariablesList, selectedItem: 1),
    actionPerformed: { model.updateTableModel()}, selectedItem: bind(target:model, targetProperty:'selected'),
    constraints: "wrap")
  panel(id: 'pnlTable', constraints: 'growx, spanx, wrap', layout: new MigLayout("insets 0")) {
    scrollPane(constraints: "growx, spanx") {
      table(model: model.createTableModel()) {
        def params = [target: current, strategy: AbstractTableComparatorChooser.SINGLE_COLUMN, source: model.tableSortingModel]
        println "table params: " + params.dump()
        TableComparatorChooser.install(params.target, params.source, params.strategy)
      }
    }
  }
  button(id: 'btnOk', text: "OK", constraints: "spanx, gapbefore push, wrap", actionPerformed: { controller.onOk()} )
}
