package com.gap.pipeline.tasks

import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import com.gap.pipeline.utils.IvyCoordinateParser
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

class CreateECLinksTask {
    def log = LogFactory.getLog(com.gap.pipeline.tasks.CreateECLinksTask)
    private project

    CreateECLinksTask(project) {
        super(project)
        this.project = project
    }

    def execute() {
        for (int i = 1; hasProperty("label" + i) && hasProperty("url" + i); i++) {
            new CommanderClient.addLinkToUrl(project.property("label"+i), project.property("url"+i), new CommanderClient.getJobId())
        }
        if(i == 1) {
            throw new IllegalArgumentException("Please provide arguments as label1,url1,label2,url2 etc")
        }
    }
}