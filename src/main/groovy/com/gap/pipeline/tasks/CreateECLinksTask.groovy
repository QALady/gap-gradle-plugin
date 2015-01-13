package com.gap.pipeline.tasks

import com.gap.pipeline.ec.CommanderClient
import org.apache.commons.logging.LogFactory

class CreateECLinksTask {
    def log = LogFactory.getLog(CreateECLinksTask)
    private project

    CreateECLinksTask(project) {
        this.project = project
    }

    def execute() {
        CommanderClient ecclient = new CommanderClient()
        for (int i = 1; project.hasProperty("label" + i) && project.hasProperty("url" + i); i++) {
            ecclient.addLinkToUrl(project.property("label"+i), project.property("url"+i))
        }
        //if(i == 1) {
        //    throw new IllegalArgumentException("Please provide arguments as label1,url1,label2,url2 etc")
        //}
    }
}