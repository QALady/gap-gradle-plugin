package com.gap.gradle.tasks

import org.gradle.api.Project

import com.gap.pipeline.tasks.WatchmenTask
import com.sun.org.apache.commons.logging.LogFactory

class PrepareToPromoteToProductionTask extends WatchmenTask {
	def log = LogFactory.getLog(PrepareToPromoteToProductionTask)

	PrepareToPromoteToProductionTask(Project project) {
		super(project)
	}

	void execute() {
		// task: promote cookbook berksfile
		//project.tasks.findByName('promoteCookbookBerksfile').execute()

		// task: promote RPMs to prod if the artifact is rpms
		log.info("checking if artifact is RPM: ${project.prodDeploy.isRPM}")
        if(project.prodDeploy.isRPM){
			log.info("executing promoteRpm task")
            project.tasks.findByName('promoteRpm').execute()
        }
	}
}
