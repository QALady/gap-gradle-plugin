package com.gap.gradle.plugins.cookbook

import com.gap.gradle.plugins.git.GitClient

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

class UpdateCookbookSHATask {
    Project project
    GitClient client
    private Log log = LogFactory.getLog(UpdateCookbookSHATask)

    UpdateCookbookSHATask(Project project){
        this.project = project
    }

    def execute(){
        parametersExist()
        checkFullRepoNameFormat()
        client = new GitClient(project.gitconfig.userId, project.gitconfig.shaId,
                project.gitconfig.fullRepoName)
        client.checkout()
        client.updateBerksfile()
        client.commitAndPush()
    }

    def parametersExist(){
        if(project.gitconfig.fullRepoName == null){
            throw new Exception('There is no fullRepoName defined. ' +
                    'Please run this gradle task with -PfullRepoName=value')
        }
        if(project.gitconfig.shaId == null){
            throw new Exception()('There is no SHA Id defined. ' +
                    'Please run this gradle task with -PshaId=value')
        }
        if(project.gitconfig.userId == null){
            throw new Exception('There is no userId defined. ' +
                    'Please run this gradle task with -PuserId=value')
        }
    }

    def checkFullRepoNameFormat(){
        if(!project.gitconfig.fullRepoName.contains("/")){
            throw new Exception("The fullRepoName must have the following format: 'organization/repoName'")
        }
    }
}
