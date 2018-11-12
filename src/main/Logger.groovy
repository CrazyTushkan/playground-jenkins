package main

import org.jenkinsci.plugins.workflow.cps.CpsThreadGroup

class Logger {
    def thread
    def script

    Logger() {
        this.thread = CpsThreadGroup.current()
    }

    // @NonCPS
    // private LogRecorder getRecorder(String recorder) {
    //     Map<String, LogRecorder> logRecorders = Jenkins.get().getLog().logRecorders
    //     return logRecorders.get(recorder)
    
    // @NonCPS
    // private void createRecorder(String name) {
    //     LogRecorder recorder = get
    //     if (!recorder) {
    //         recorder = new LogRecorder(name)
    //         logRecorders.put(name, recorder)
    //     }
    // }

    // @NonCPS
    // private void createTarget(String recorderName) {
    //     Map<String, LogRecorder> logRecorders = Jenkins.get().getLog().logRecorders
    //     LogRecorder recorder = logRecorders.get(name)
        
    // }
    
    @NonCPS
    public void log(logLevel, message) {
        def stream = this.thread.execution.owner.listener.getLogger()
        stream.println(message)
    }
}

// TODO: Optionally write to system log; Pass destination on creation;
// TODO: Check if it is possible (and easy enough if possible) to create system log
