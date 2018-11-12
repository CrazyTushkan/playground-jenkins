import org.junit.Before
import org.junit.Test
import com.lesfurets.jenkins.unit.BasePipelineTest
import com.lesfurets.jenkins.unit.BaseRegressionTest
import com.lesfurets.jenkins.unit.RegressionTestHelper

class TestJenkinsfile extends BasePipelineTest {
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        binding.setVariable('scm', [
            $class: 'GitSCM',
            branches: [[name: 'master']],
            doGenerateSubmoduleConfigruations: false,
            extensions: [],
            submoduleCfg: [],
            userRemoteConfigs: [[url: "/var/git-repo"]]
        ])
    }
    
    @Test
    public void executed_correctly() throws Exception {
        runScript("Jenkinsfile")
        printCallStack()
    }

}

class RegressionJenkinsfile extends BaseRegressionTest {
    @Override 
    @Before
    public void setUp() throws Exception {
        System.setProperty(RegressionTestHelper.PIPELINE_STACK_WRITE, "true");
        super.setUp();
        binding.setVariable('scm', [
            $class: 'GitSCM',
            branches: [[name: 'master']],
            doGenerateSubmoduleConfigruations: false,
            extensions: [],
            submoduleCfg: [],
            userRemoteConfigs: [[url: "/var/git-repo"]]
        ])
    }
    
    @Test
    public void executed_no_regression() throws Exception {
        println(System.getProperty("pipeline.stack.write"))
        runScript("Jenkinsfile")
        testNonRegression()
    }
}
