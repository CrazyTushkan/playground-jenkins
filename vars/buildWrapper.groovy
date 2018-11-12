import java.util.logging.Level;
import java.util.logging.Logger;

def call(Closure body) {
    def config = [:]

    if (body != null) {
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
    }

    def settings = config.settings ?: "settings.xml"
    node() {
        stage('Checkout') {
            MyLogger.setUp()
            private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            echo "Checkout"
            LOGGER.info("Checkout")
            checkout scm
        }

        stage('Main') {
            // Test Python setup
            sh(script: 'python -c "import requests"', returnStatus: true)
            // Test Docker setup
            sh 'docker version'
        }

        stage('Post') {
            // Print info of standard tools
            sh 'ls -al'
            sh 'java -version'
            sh "mvn -s $settings -version"
            sh 'python -V'
        }
    }
}
