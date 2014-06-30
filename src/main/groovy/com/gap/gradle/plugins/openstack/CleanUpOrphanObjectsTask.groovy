package com.gap.gradle.plugins.openstack

import com.gap.gradle.jenkins.JenkinsClient
import com.gap.gradle.jenkins.JenkinsRunner
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import org.gradle.api.Project


@RequiredParameters([
@Require(parameter = 'jenkins.knifeServerUrl', description = "Jenkins Server URL for triggering jobs that invoke knife commands"),
@Require(parameter = 'jenkins.knifeUser', description = "Jenkins User ID to trigger job"),
@Require(parameter = 'jenkins.knifeAuthToken', description = "Jenkins API Auth token to trigger job."),
@Require(parameter = 'jenkins.knifeCleanUpJobName', description = "Job to be triggered"),
@Require(parameter = 'nodeToDelete', description = "this is a project property expected on the command line, to specify which node needs to be deleted on open stack eg: -PnodeToDelete=xxx")
])
class CleanUpOrphanObjectsTask extends WatchmenTask  {
    private Project project
    
    CleanUpOrphanObjectsTask(project) {
        super(project)
        this.project = project
    }

    def execute() {
        super.validate()
        cleanUpOrphanObjects()
    }

    def cleanUpOrphanObjects() {
        def jenkinsConfig = project.jenkins
        JenkinsClient jenkinsClient = new JenkinsClient(jenkinsConfig.knifeServerUrl, jenkinsConfig.knifeUser, jenkinsConfig.knifeAuthToken)
        JenkinsRunner jRunner = new JenkinsRunner(jenkinsClient)
      	def jobName = jenkinsConfig.knifeCleanUpJobName
        def jobParams = [:]
        jobParams.put("NODE_NAME", project.nodeToDelete)
        jRunner.runJob(jobName,jobParams)
    }
}
