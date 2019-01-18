package main

import org.jenkinsci.plugins.workflow.cps.CpsThreadGroup
import hudson.logging.LogRecorder
import jenkins.model.Jenkins
import java.util.logging.Logger as JavaLogger
import java.util.logging.Level


class Logger {
    def thread

    Logger() {
        this.thread = CpsThreadGroup.current()
        createRecorder('TestRecorder')
        createTarget('TestRecorder', 'TestTarget', Level.ALL)
        JavaLogger.getLogger('TestTarget').info('Test message')
    }

    @NonCPS
    private Map<String, LogRecorder> getRecorders() {
        return Jenkins.get().getLog().logRecorders
    }
    
    @NonCPS
    private void createRecorder(String name) {
        Map<String, LogRecorder> recorders = getRecorders()
        if(!recorders.get(name)) {
            LogRecorder recorder = new LogRecorder(name)
            recorders.put(name, recorder)
            log('LogRecorder created')
        }
    }

    @NonCPS
    private void createTarget(String recorderName, String targetName, Level targetLevel) {
        LogRecorder recorder = getRecorders().get(recorderName)
        LogRecorder.Target newTarget = new LogRecorder.Target(targetName, targetLevel)
        List<LogRecorder.Target> targets = new ArrayList<LogRecorder.Target>(recorder.targets.getView())
        for(target in recorder.targets.getView()) {
            if(target.getName() == targetName) {
                targets.remove(target)
            }
        }
        targets.add(newTarget)
        recorder.targets.replaceBy(targets)
        log('LogRecorder targets updated')
    }
    
    @NonCPS
    public void log(message) {
        def stream = this.thread.execution.owner.listener.getLogger()
        stream.println(message)
    }
}

// TODO: Optionally write to system log; Pass destination on creation;
// TODO: Check if it is possible (and easy enough if possible) to create system log
