pipelineJob('test-pipeline-build') {
	description('Build test docker image, test and push it to local registry')
	definition {
    	cpsScm {
      	scm {
          git {
            branch('origin/master')
            remote {
              url('git@github.com:CrazyTushkan/playground-jenkins.git')
              credentials('CrazyTushkan')
            }
          }
      	}
      	scriptPath('Jenkinsfile')
    	}
    }
}
