package com.gap.gradle.tasks

import org.gradle.api.Project

import com.gap.pipeline.tasks.WatchmenTask

class PrepareToPromoteToProductionTask extends WatchmenTask {
//	def log = LogFactory.getLog(PrepareToPromoteToProductionTask)

	PrepareToPromoteToProductionTask(Project project) {
		super(project)
	}

	void execute() {
		// task: promote cookbook berksfile
		//project.tasks.findByName('promoteCookbookBerksfile').execute()
	}
}
