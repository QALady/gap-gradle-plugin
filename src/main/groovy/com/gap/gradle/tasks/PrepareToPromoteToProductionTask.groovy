package com.gap.gradle.tasks

import org.gradle.api.Project

class PrepareToPromoteToProductionTask extends WatchmenTask {

	PrepareToPromoteToProductionTask(Project project) {
		super(project)
		this.project = project
	}

	void execute() {
		// do something
		// TODO: here we need to integrate the piece of calling the Git upload task to 
		// update the Cookbook sha1ID into Berksfile.prod.
	}
}
