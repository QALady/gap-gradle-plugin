package com.gap.gradle.tasks

import com.gap.gradle.git.GitClient
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

@RequiredParameters([
    @Require(parameter = 'git.fullRepoName', description = "Full repo name of cookbook to be promoted."),
    @Require(parameter = 'git.sha1Id', description = "SHA1 ID of cookbook repository.")
])
class UpdateCookbookSHATask extends WatchmenTask{
    Project project
    GitClient client
    CommanderClient commanderClient = new CommanderClient()

    UpdateCookbookSHATask(Project project){
        super(project)
        this.project = project
    }

    def execute(){
        super.validate()
        checkFullRepoNameFormat()		
        client = new GitClient(commanderClient.getUserId(), project.git.sha1Id,
        project.git.fullRepoName)
        client.checkout()
        client.updateBerksfile()
        client.commitAndPush()
    }

    def checkFullRepoNameFormat(){
        if(!project.git.fullRepoName.contains("/")){
            throw new Exception("The fullRepoName must have the following format: 'organization/repoName'")
        }
    }
}
