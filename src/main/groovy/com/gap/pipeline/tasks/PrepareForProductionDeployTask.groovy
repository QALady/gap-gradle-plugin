package com.gap.pipeline.tasks

import com.gap.pipeline.ProdDeployParameterConfig
import com.gap.pipeline.ProdPrepareConfig
import com.gap.pipeline.ec.CommanderArtifacts
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.exception.InvalidSHA1IDException
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import groovy.json.JsonBuilder
import org.apache.commons.logging.LogFactory

@RequiredParameters([
    @Require(parameter = 'prodPrepare.deployECProcedure', description = "EC procedure to trigger(which does actual deployment) on successful run."),
    @Require(parameter = 'artifactCoordinates', description = "location of artifacts, to pass to prodDeploy job"),
	@Require(parameter = 'prodPrepare.appVersion', description = "Version of the application that should be deployed in the prod node."),
	@Require(parameter = 'prodPrepare.cookbookName', description = "Application cookbook name"),
])
class PrepareForProductionDeployTask extends WatchmenTask {
    def log = LogFactory.getLog(com.gap.pipeline.tasks.PrepareForProductionDeployTask)
    def project

    PrepareForProductionDeployTask(project){
        super(project)
        this.project = project
    }

    def execute(){
        log.info("Executing task prepareForProductionDeploy...")
        log.info("Sha1 ID(s) - ${project.prodPrepare.sha1Ids}")
        log.info("Role Name - ${project.prodPrepare.roleName}")
        log.info("Cookbook Name -  ${project.prodPrepare.cookbookName}")
		log.info("Cookbook Sha1-  ${project.prodPrepare.cookbookSha1Id}")
        log.info("Deploy EC Procedure -  ${project.prodPrepare.deployECProcedure}")
        log.info("Deployment Nodes - ${project.prodPrepare.nodes}")
		log.info("Application deploy version - ${project.prodPrepare.appVersion}")

        validate()

        createProdDeployJsonArtifact()
        log.info("Publishing links to artifacts in EC ...")
        copyArtifactsForUseByEC()
        publishArtifactLinksToEC()
        log.info("Uploading artifacts to Artifactory...")
        new UploadBuildArtifactsTask(project).execute()
        log.info("Updating Artifact Coordinates in the Deploy Procedure... ")
        def commanderClient = new CommanderClient()
        commanderClient.setDefaultParameterValue(project.prodPrepare.deployECProcedure,'artifactCoordinates', project.artifactCoordinates)
        log.info("Creating links in Electric Commander...")
        commanderClient.addLinkToRunProcedureInJob("Run ${project.prodPrepare.deployECProcedure}", project.prodPrepare.deployECProcedure)
    }

    private void createProdDeployJsonArtifact() {
        log.info("Creating Prod Deploy Json Artifact...")
        project.prodPrepare.sha1IdList = splitOnCommas(project.prodPrepare.sha1Ids)
        project.prodPrepare.rpmNames = splitOnCommas(project.prodPrepare.rpmNames)
        def jsonBuilder = new JsonBuilder(new ProdDeployParameterConfig(project.prodPrepare))
        def fileWriter = new FileWriter("${project.buildDir}/artifacts/${ProdPrepareConfig.FILE_NAME}")
        jsonBuilder.writeTo(fileWriter)
        fileWriter.close()
    }
    private void copyArtifactsForUseByEC () {
        new CommanderArtifacts(new CommanderClient()).copyToArtifactsDir("${project.buildDir}/artifacts/${ProdPrepareConfig.FILE_NAME}")
    }


    private void publishArtifactLinksToEC() {
        def artifacts = new CommanderArtifacts(new CommanderClient());
        artifacts.publishLinks()
    }

    def validate() {
        super.validate()
		def sha1IdList = splitOnCommas(project.prodPrepare.sha1Ids)
        sha1IdList.findAll {
            if(!(it ==~ ProdPrepareConfig.SHA1_PATTERN)) {
                throw new InvalidSHA1IDException("Invalid SHA1 id: ${it}")
            }
            return true
        }
		if (!(project.prodPrepare.cookbookSha1Id ==~ ProdPrepareConfig.SHA1_PATTERN)) {
			throw new InvalidSHA1IDException("Invalid SHA1 id: ${project.prodPrepare.cookbookSha1Id}")
		}
    }

    private def splitOnCommas(stringToSplit) {
        if (stringToSplit) {
            def parts = []
            stringToSplit.eachLine { line ->
                line.split(',').each { item ->
                    if (item.trim()) {
                        parts << item.trim()
                    }
                }
            }
            parts
        } else {
            []
        }
    }
}