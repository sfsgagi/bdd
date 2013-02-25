package bdd

import ca.odell.glazedlists.BasicEventList 
import ca.odell.glazedlists.SortedList 
import ca.odell.glazedlists.gui.AbstractTableComparatorChooser 
import ca.odell.glazedlists.swing.EventTableModel 
import ca.odell.glazedlists.swing.TableComparatorChooser 
import griffon.glazedlists.gui.DefaultWritableTableFormat;
import groovy.swing.SwingBuilder 
import java.awt.BorderLayout 

class FunctionDialogController {
    // these will be injected by Griffon
    def model
    def view
     

    // void mvcGroupDestroy() {
    //    // this method is called when the group is destroyed
    // }

    /*
    def action = { evt = null ->
    }
    */
	
	def onOk = {
		view.functionDialog.visible = false
		def nLevels = view.cmbNVariables.selectedItem
		app.views.bdd.pnlReorder.changeNVarsTo(nLevels)
		app.controllers.bdd.redraw(model.tableModel, nLevels)
	}
	
	def changeNumberOfVariables = {
		int nVars = view.cmbNVariables.selectedItem
		
		def rows = (0..(2**nVars - 1)).collect {
			String strRow = Integer.toString (it, 2).padLeft(nVars, '0')
			def row = [:]
			(1..nVars).each {
				row['x' + it] = strRow[it - 1]
			}
			row['f'] = false
			row
		}
		
		def tableModel = new SortedList(new BasicEventList(),
			{a, b -> a.x1 <=> b.x1} as Comparator)
		
		def columns = (1..nVars).collect {
			[name: 'x' + it, editable: { target, columns, index -> false }]
		}
		columns.add([name: 'f', class: Boolean.class])
		
		tableModel.addAll(rows)
		
		model.tableModel = tableModel
				
		def createTableModel = {
			def columnNames = columns.collect{ it.name };
			return new EventTableModel(tableModel, new DefaultWritableTableFormat(columns))
		}
		
		def swing = new SwingBuilder()
		def scp = swing.scrollPane(constraints: "growx, spanx") {
			table(model: createTableModel()) {
				def params = [target: current, strategy: AbstractTableComparatorChooser.SINGLE_COLUMN, source: tableModel]
				TableComparatorChooser.install(params.target, params.source, params.strategy)
			}
		}
				
		view.pnlTable.removeAll()
		view.pnlTable.add(scp)
		view.pnlTable.revalidate()
		view.pnlTable.repaint()
	}
}
