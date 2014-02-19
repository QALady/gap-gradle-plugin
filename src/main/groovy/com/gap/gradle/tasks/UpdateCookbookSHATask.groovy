package com.gap.gradle.tasks

import com.gap.gradle.git.GitClient
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

@RequiredParameters([
    @Require(parameter = 'gitconfig.fullRepoName', description = "Full repo name of cookbook to be promoted."),
    @Require(parameter = 'gitconfig.shaId', description = "SHA1 ID of cookbook repository."),
    @Require(parameter = 'gitconfig.userId', description = "Git user id")
])
class UpdateCookbookSHATask extends WatchmenTask{
    Project project
    GitClient client
    private Log log = LogFactory.getLog(UpdateCookbookSHATask)

    UpdateCookbookSHATask(Project project){
        super(project)
        this.project = project
    }

    def execute(){
        super.validate()
        checkFullRepoNameFormat()
        client = new GitClient(project.gitconfig.userId, project.gitconfig.shaId,
                project.gitconfig.fullRepoName)
        client.checkout()
        client.updateBerksfile()
        client.commitAndPush()
    }

    def checkFullRepoNameFormat(){
        if(!project.gitconfig.fullRepoName.contains("/")){
            throw new Exception("The fullRepoName must have the following format: 'organization/repoName'")
        }
    }
}
