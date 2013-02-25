package bdd

import javax.swing.JPanel
import net.miginfocom.swing.MigLayout

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
	comboBox(id: 'cmbNVariables', items: 1..4, selectedIndex: 3, actionPerformed: controller.changeNumberOfVariables, constraints: 'wrap')
	panel(id: 'pnlTable', constraints: 'growx, spanx, wrap', layout: new MigLayout("insets 0"))
	button(id: 'btnOk', text: "OK", constraints: "spanx, gapbefore push, wrap", actionPerformed: { controller.onOk()} )
}

controller.changeNumberOfVariables()