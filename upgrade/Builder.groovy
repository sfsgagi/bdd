root {
    'groovy.swing.SwingBuilder' {
        controller = ['Threading']
        view = '*'
    }
    'griffon.app.ApplicationBuilder' {
        view = '*'
    }
}
root.'griffon.builder.trident.TridentBuilder'.view = '*'



root.'MiglayoutGriffonAddon'.addon=true

root.'GlazedlistsGriffonAddon'.addon=true
