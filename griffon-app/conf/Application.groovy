application {
    title = 'Bdd'
    startupGroups = ['bdd', 'FunctionDialog']

    // Should Griffon exit when no Griffon created frames are showing?
    autoShutdown = true

    // If you want some non-standard application class, apply it here
    //frameClass = 'javax.swing.JFrame'
}
mvcGroups {
    // MVC Group for "FunctionDialog"
    'FunctionDialog' {
        model = 'bdd.FunctionDialogModel'
        controller = 'bdd.FunctionDialogController'
        view = 'bdd.FunctionDialogView'
    }

    // MVC Group for "bdd"
    'bdd' {
        model = 'bdd.BddModel'
        controller = 'bdd.BddController'
        view = 'bdd.BddView'
    }

}
