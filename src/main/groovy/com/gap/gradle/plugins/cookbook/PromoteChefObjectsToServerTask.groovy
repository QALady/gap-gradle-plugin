package com.gap.gradle.plugins.cookbook

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory
import org.apache.jasper.compiler.Node.ParamsAction;
import org.gradle.api.Project

import com.gap.gradle.jenkins.JenkinsClient;
import com.gap.gradle.jenkins.JenkinsException;
import com.gap.gradle.jenkins.JenkinsRunner;

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
		JenkinsClient jClient = new JenkinsClient(jConfig.serverUrl, jConfig.user, jConfig.authToken)
		JenkinsRunner jRunner = new JenkinsRunner(jClient)
		jRunner.runJob(jConfig.jobName, jobParams)
	}

	def requireJenkinsConfig() {
		if (!project.jenkins.serverUrl) {
			throw new Exception("No jenkins url configured")
		} else if (!project.jenkins.user) {
			throw new Exception("No jenkins user configured")
		} else if (!project.jenkins.authToken) {
			throw new Exception("No jenkins auth-token configured")
		} else if (!project.jenkins.jobName) {
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

}
