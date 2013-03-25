package bdd

import graph.ReorderPanel;
import java.awt.BorderLayout 
import java.awt.Color 
import javax.swing.JSplitPane 

actions {
  action(id: 'openFunctionDialogAction',
  name: 'Define function',
  mnemonic: 'D',
  accelerator: shortcut('D'),
  closure: controller.openFunctionDialog)
  action(id: 'reduceGraphAction',
  name: 'Reduce Graph',
  mnemonic: 'R',
  accelerator: shortcut('R'),
  closure: controller.onReduceGraph)
  action(
    id: 'nextStepAction',
    name: '>',
    mnemonic: '+',
    accelerator: shortcut('+'),
    closure: controller.nextStep)
  action(
    id: 'lastStepAction',
    name: '>>',
    mnemonic: 'E',
    accelerator: shortcut('E'),
    closure: controller.lastStep)
  action(
    id: 'previousStepAction',
    name: '<',
    mnemonic: '-',
    accelerator: shortcut('-'),
    closure: controller.previousStep)
  action(
    id: 'firstStepAction',
    name: '<<',
    mnemonic: 'H',
    accelerator: shortcut('H'),
    closure: controller.firstStep)
}

graphPanel = panel(id: "mainPanel", background: Color.white, layout: new BorderLayout())

statusPanel = scrollPane() {
  textArea(id: 'txaStatus', rows: 5)
}

reorderPanel = new ReorderPanel(4, controller)


application(title: 'Binary Decision Diagrams - Step by step reduction',
size: [1000,800],
//pack: true,
//location: [50,50],
locationRelativeTo: null,
iconImage: imageIcon('/griffon-icon-48x48.png').image,
iconImages: [
imageIcon('/griffon-icon-48x48.png').image,
imageIcon('/griffon-icon-32x32.png').image,
imageIcon('/griffon-icon-16x16.png').image
]) {
  borderLayout()
  panel(constraints: NORTH) {
    migLayout()
// temp button
    button("Test", actionPerformed: {controller.onTest()}, constraints: "gapright 20px")

    button(openFunctionDialogAction, constraints: "gapright 20px")
    button(reduceGraphAction, enabled: bind {model.functionDefined && model.graphDrawn})
    button("Redraw", actionPerformed: { controller.redrawReordered()}, 
    enabled: bind {model.functionDefined}, constraints: "gapright 20px")
    button (firstStepAction, id: 'btnFirst', enabled: bind {model.currentStep > 0} )
    button (previousStepAction, id: 'btnPrevious', enabled: bind {model.currentStep > 0} )
    button (nextStepAction, id: 'btnNext', enabled: bind {model.currentStep != -1 && model.currentStep < model.nSteps - 1})
    button (lastStepAction, id: 'btnLast', enabled: bind {model.currentStep != -1 && model.currentStep < model.nSteps - 1})
    label ('Step: ')
    label (text: bind {if(model.currentStep == -1) return "" else return model.currentStep + 1}, constraints: 'wrap')
    panel(id: "pnlReorderContainer", constraints: "spanx") {
      borderLayout()
      widget(reorderPanel, id: "pnlReorder", visible: bind {model.functionDefined}, constraints: CENTER)
    }
    label (text: "[You can reorder variables (use drag & drop)]", visible: bind {model.functionDefined}, constraints: 'spanx, wrap')
  }
  splitPane(topComponent: graphPanel, bottomComponent: statusPanel,
  dividerLocation: 750, dividerSize: 3, orientation: JSplitPane.VERTICAL_SPLIT, constraints: CENTER)
}
