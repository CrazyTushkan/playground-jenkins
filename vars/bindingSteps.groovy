import main.CustomSteps

def foo = 'foo'

def date() {
    println foo
    def customSteps = new CustomSteps()
    customSteps.date()
}
