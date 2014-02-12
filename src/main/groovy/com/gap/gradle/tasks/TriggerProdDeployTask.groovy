package com.gap.gradle.tasks

import org.gradle.api.Project

import com.gap.gradle.ProdDeployConfig
import groovy.json.JsonSlurper
import groovy.io.*

class TriggerProdDeployTask extends WatchmenTask {
	private Project project
	private ProdDeployConfig deployConfig

	TriggerProdDeployTask(Project project) {
		super(project)
		this.project = project
	}

	void execute() {
		// read the json file from the prodDeployParameterJsonPath param into ProdDeployConfig
		deployConfig = loadConfigFromJson()
		// promoteChefObjectsToprodServer. (involves looping thru given sha1 ids
		promoteChefObjectsToProdServer()
		// promoteCookbookVersion to Prod Server.
		publishCookbookToProdChefServer()
		// call the task that runs chef-client on all the prod nodes. 
	}
	
	def loadConfigFromJson() {
        deployConfig = new JsonSlurper().parse(new File(deployConfig.PARAMJSON))
        deployConfig.each { println it }
	}
	
	def promoteChefObjectsToProdServer() {
        PromoteChefObjectsToServerTask chefObjectsTask = new PromoteChefObjectsToServerTask(this.project)
        for (sha1Id in deployConfig.sha1IdList) {
            chefObjectsTask.execute()
        }
	}

	def publishCookbookToProdChefServer() {
		
	}
}
