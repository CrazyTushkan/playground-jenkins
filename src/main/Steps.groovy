package main

import org.jenkinsci.plugins.workflow.cps.DSL
import org.jenkinsci.plugins.workflow.cps.CpsThread
import com.cloudbees.groovy.cps.NonCPS

class Steps implements Serializable {
    static DSL steps = getSteps()

    @NonCPS
    private static getSteps() {
        if (!steps) {
            CpsThread c = CpsThread.current()
            return new DSL(c.getExecution().getOwner())
        }
    }
}
