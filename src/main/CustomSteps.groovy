package main

import org.jenkinsci.plugins.workflow.cps.DSL
import org.jenkinsci.plugins.workflow.cps.CpsThread

class CustomSteps extends Steps {

    static void date () {
        steps.sh('date')
    }
}
