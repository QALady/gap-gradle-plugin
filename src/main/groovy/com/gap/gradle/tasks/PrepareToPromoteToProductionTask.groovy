package com.gap.gradle.tasks

import org.gradle.api.Project
import com.gap.pipeline.tasks.WatchmenTask

class PrepareToPromoteToProductionTask extends WatchmenTask {

	PrepareToPromoteToProductionTask(Project project) {
		super(project)
	}

	void execute() {
		project.gitconfig.userId = project.getProperty("userId")
		println "USER ID ========== " + project.gitconfig.userId
		project.gitconfig.fullRepoName = project.prodDeploy.cookbook.name
		project.gitconfig.shaId = project.prodDeploy.cookbook.sha1Id
		project.tasks.findByName('promoteCookbookBerksfile').execute()
        if(project.prodDeploy.isRpm){
            project.tasks.findByName('promoteRpm').execute()
        }
	}
}
