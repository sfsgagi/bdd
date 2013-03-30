log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '%d [%t] %-5p %c - %m%n'), threshold: org.apache.log4j.Level.ERROR
        rollingFile name:'file', file:'test.log', maxFileSize: '1MB', layout: pattern(conversionPattern: '%d [%t] %-5p %c - %m%n'), threshold: org.apache.log4j.Level.ALL
    }

    root {
      all 'stdout', 'file'
    }

    error  'org.codehaus.griffon'

    info   'griffon.util',
           'griffon.core',
           'griffon.@application.toolkit@',
           'griffon.app'

}

