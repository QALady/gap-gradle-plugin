package com.gap.gradle.tasks

import org.gradle.api.Project

import com.gap.gradle.ProdDeployConfig


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
		
	}
	
	def promoteChefObjectsToProdServer() {
		
	}

	def publishCookbookToProdChefServer() {
		
	}
}
