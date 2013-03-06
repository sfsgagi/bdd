package bdd

import ca.odell.glazedlists.*
import ca.odell.glazedlists.swing.*
import groovy.beans.Bindable
import griffon.glazedlists.gui.DefaultWritableTableFormat;


class FunctionDialogModel {
  // @Bindable String propName
  def tableSortingModel
  def tableModel
  final EventList nVariablesList = new BasicEventList()
  @Bindable Integer nVars = 1

  def createTableModel() {
    println "Creating table model..."
    def list = new SortedList(new BasicEventList(), {a, b -> a.x1 <=> b.x1} as Comparator)
    list.addAll(createRows())

    tableSortingModel = list

    tableModel = new EventTableModel(list, new DefaultWritableTableFormat(createColumns()))
    tableModel
  }

  def createRows() {
    def rows = (0..(2**nVars - 1)).collect {
      String strRow = Integer.toString (it, 2).padLeft(nVars, '0')
      def row = [:]
      (1..nVars).each {
        row['x' + it] = strRow[it - 1]
      }
      row['f'] = false
      row
    }

    rows.dump()
    rows
  }

  def createColumns() {
    def columns = (1..nVars).collect {
      [name: 'x' + it, editable: { target, columns, index -> false }]
    }
    columns.add([name: 'f', class: Boolean.class])
    columns
  }

  def updateTableModel() {
    println "Updating table model..."
    tableSortingModel.clear()
    tableSortingModel.addAll(createRows())

    tableModel.setTableFormat(new DefaultWritableTableFormat(createColumns()))
  }
}
