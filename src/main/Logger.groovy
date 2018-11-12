package main

class Logger implements Serializable {
    static Script script
    static def context
    Logger(Script script) {
        this.script = script
        this.context = script.getContext(TaskListener.class)
    }
    @NonCPS
    public static void log(message) {
        def logger = this.context.getLogger()
        logger.println(message)
        println("println in logger")
    }
}
