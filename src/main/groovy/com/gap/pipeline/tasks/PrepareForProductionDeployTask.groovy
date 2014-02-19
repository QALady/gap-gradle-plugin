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
    @Require(parameter = 'prodPrepare.sha1Ids', description = "SHA ID's of the chef objects to promote"),
	@Require(parameter = 'artifactCoordinates', description = "location of artifacts, to pass to prodDeploy job"),
	@Require(parameter = 'prodPrepare.appVersion', description = "Version of the application that should be deployed in the prod node."),
	@Require(parameter = 'prodPrepare.cookbookSha1Id', description = "Application promoted cookbook sha1Id in git"),
	@Require(parameter = 'prodPrepare.cookbookName', description = "Application cookbook name"),
    @Require(parameter = 'prodPrepare.rpmName', description = "Name of he rpm with extension, version and arch"),
    @Require(parameter = 'prodPrepare.yumSourceUrl', description = "Dev yum repo url until channel to download an rpm from"),
    @Require(parameter = 'prodPrepare.yumDestinationUrl', description = "Prod yum repo url until channel to upload an rpm to")
])
class PrepareForProductionDeployTask extends WatchmenTask {
    def log = LogFactory.getLog(com.gap.pipeline.tasks.PrepareForProductionDeployTask)
    def project
	def sha1IdList = [] // empty array to start with

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
		log.info("Is the deployable RPM? - ${project.prodPrepare.isRPM}")
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
        project.prodPrepare.sha1IdList =	sha1IdList
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
		sha1IdList = processSha1Ids()
        sha1IdList = sha1IdList.findAll {
            if(!(it ==~ ProdPrepareConfig.SHA1_PATTERN)) {
                throw new InvalidSHA1IDException("Invalid SHA1 id: ${it}")
            }
            return true
        }
		if (!(project.prodPrepare.cookbookSha1Id ==~ ProdPrepareConfig.SHA1_PATTERN)) {
			throw new InvalidSHA1IDException("Invalid SHA1 id: ${project.prodPrepare.cookbookSha1Id}")
		}
    }

	private def processSha1Ids() {
		if (project.prodPrepare.sha1Ids) {
			def sha1Ids = []
			project.prodPrepare.sha1Ids.eachLine { line ->
				line.split(',').each { sha1 ->
					if (sha1.trim()) {
						sha1Ids << sha1.trim()
					}
				}
			}
			sha1Ids
		} else {
			[]
		}
	}
}
