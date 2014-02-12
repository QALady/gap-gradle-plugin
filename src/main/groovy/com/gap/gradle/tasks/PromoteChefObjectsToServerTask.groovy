package com.gap.gradle.tasks

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory
import org.apache.jasper.compiler.Node.ParamsAction;
import org.gradle.api.Project

import com.gap.gradle.jenkins.JenkinsClient;
import com.gap.gradle.jenkins.JenkinsException;
import com.gap.gradle.jenkins.JenkinsRunner;
import com.gap.gradle.ProdDeployConfig;

class PromoteChefObjectsToServerTask {
	private Project project
	private def jobParams = [:] // empty map to start with
	private Log log = LogFactory.getLog(PromoteChefObjectsToServerTask)

	PromoteChefObjectsToServerTask(Project project) {
		this.project = project
	}
	
	def execute() {
		requireJenkinsConfig()
		requireIncludeParameters()
		promoteChefObjectsByCallingJenkinsJob()
	}

	def promoteChefObjectsByCallingJenkinsJob() {
		def jConfig = project.jenkins
		JenkinsClient jClient = new JenkinsClient(jConfig.knifeServerUrl, jConfig.knifeUser, jConfig.knifeAuthToken)
		JenkinsRunner jRunner = new JenkinsRunner(jClient)
		jRunner.runJob(jConfig.knifeJobName, jobParams)
	}

	def requireJenkinsConfig() {
		if (!project.jenkins.knifeServerUrl) {
			throw new Exception("No jenkins url configured")
		} else if (!project.jenkins.knifeUser) {
			throw new Exception("No jenkins user configured")
		} else if (!project.jenkins.knifeAuthToken) {
			throw new Exception("No jenkins auth-token configured")
		} else if (!project.jenkins.knifeJobName) {
			throw new Exception("No jenkins jobName configured")
		}
	}
	
	def requireIncludeParameters() {
		if (project.parameters == null) {
			throw new Exception("No parameters passed to project!")
		}
		def parameterNames = project.parameters.parameterNames
		log.info("Validating if the required parameters defined in parameterNames property exist? = " + parameterNames ?: "No parameters configured to this Job.")
		parameterNames.split(",").each { parameter ->
			if (project.parameters[parameter] == null) {
				throw new JenkinsException("This build job need " + parameter + " and is missing it. Please configure this parameter and try running again.")
			}
			jobParams.put(parameter, project.parameters[parameter])
		}
		log.info("All Jenkins build parameters validated and loaded.")
	}

    def loadDeployConfig(){
        def config = new ProdDeployConfig()

    }
}
