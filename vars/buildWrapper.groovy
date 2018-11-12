// import java.util.logging.Level;
// import java.util.logging.Logger;
import main.Logger
import org.jenkinsci.plugins.workflow.cps.CpsThreadGroup

def call(Closure body) {
    // def config = [:]

    // if (body != null) {
    //     body.resolveStrategy = Closure.DELEGATE_FIRST
    //     body.delegate = config
    //     body()
    // }

    // def settings = config.settings ?: "settings.xml"

    node() {
        stage('Checkout') {
            echo 'lib echo'
            println 'lib println'
            customLogger = new Logger(CpsThreadGroup.current(), this)
            customLogger.log(1, 'Library log')
        }

        // stage('Main') {
        //     // Test Python setup
        //     sh(script: 'python -c "import requests"', returnStatus: true)
        //     // Test Docker setup
        //     sh 'docker version'
        // }

        // stage('Post') {
        //     // Print info of standard tools
        //     sh 'ls -al'
        //     sh 'java -version'
        //     sh "mvn -s $settings -version"
        //     sh 'python -V'
        // }
    }
}
