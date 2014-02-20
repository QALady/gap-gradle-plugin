package com.gap.gradle.tasks

import org.gradle.api.Project
import com.gap.pipeline.tasks.WatchmenTask

class PrepareToPromoteToProductionTask extends WatchmenTask {

	PrepareToPromoteToProductionTask(Project project) {
		super(project)
	}

	void execute() {
		// task: promote cookbook berksfile
		project.tasks.findByName('promoteCookbookBerksfile').execute()

		// task: promote RPMs to prod if the artifact is rpms
        if(project.prodDeploy.isRpm){
            project.tasks.findByName('promoteRpm').execute()
        }
	}
}
