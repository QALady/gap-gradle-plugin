package com.gap.pipeline.tasks

import org.gradle.api.Project
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import com.gap.pipeline.ec.SegmentRegistry
import com.gap.gradle.ivy.IvyInfo




class PopulateSegmentRegistryTask {

    Log log = LogFactory.getLog(com.gap.pipeline.tasks.PopulateSegmentRegistryTask)
    Project project
    Object registry

    PopulateSegmentRegistryTask(Project project) {
        PopulateSegmentRegistryTask(project, new SegmentRegistry())
    }

    PopulateSegmentRegistryTask(Project project, SegmentRegistry registry){
        this.project = project
        this.registry = registry
    }

    def execute(){
        registry.populate(new IvyInfo(project))
    }

}
