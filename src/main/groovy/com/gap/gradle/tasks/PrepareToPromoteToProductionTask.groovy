package com.gap.gradle.tasks

import org.gradle.api.Project

class PrepareToPromoteToProductionTask extends WatchmenTask {

	PrepareToPromoteToProductionTask(Project project) {
		super(project)
	}

	void execute() {
		project.gitconfig.userId = System.getProperty("userId")
		println "USER ID ========== " + project.gitconfig.userId
		project.gitconfig.fullRepoName = project.prodDeploy.cookbook.name
		project.gitconfig.sha1Id = project.prodDeploy.cookbook.sha1Id
		project.tasks.findByName('promoteCookbookBerksfile').execute()
	}
}
