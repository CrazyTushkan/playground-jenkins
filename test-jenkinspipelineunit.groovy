@Grab(group='com.lesfurets', module='jenkins-pipeline-unit', version='1.0')

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Test
import org.junit.Before

class TestExampleJob extends BasePipelineTest {
    @Before
    void setUp() {
        def library = library()
            .name('buildWrapper')
            .retriever(localSource("/home/groovy/scripts/"))
            .targetPath(./)
            .build()
	super.setUp()
	println("SetUp")
    }

    @Test
    void Test1() throws Exception {
        println("Test1")
	def script = loadScript("/home/groovy/scripts/Jenkinsfile")
        script.execute()
        printCallStack()
    }
}
