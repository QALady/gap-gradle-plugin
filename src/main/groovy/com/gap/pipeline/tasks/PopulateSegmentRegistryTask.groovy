package com.gap.pipeline.tasks
import com.gap.gradle.ivy.IvyInfo
import com.gap.pipeline.ec.SegmentRegistry
import org.apache.commons.logging.LogFactory

class PopulateSegmentRegistryTask {

    def log = LogFactory.getLog(com.gap.pipeline.tasks.PopulateSegmentRegistryTask)
    def project
    def registry

    PopulateSegmentRegistryTask(project, registry = new SegmentRegistry()){
        this.project = project
        this.registry = registry
    }

    def execute(){
        registry.populate(new IvyInfo(project))
    }

}
