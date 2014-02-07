package com.gap.gradle.plugins.git

import com.gap.gradle.utils.ShellCommand
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

/**
 * Created by ccaceres on 2/5/14.
 */
class GitCommitAndPushTask {

    Project project
    Log log = LogFactory.getLog(GitCommitAndPushTask)
    def fullRepo
    def repo
    def org
    def gitPath
    def currentPath
    def file

    GitCommitAndPushTask(Project project){
        this.project = project
        parametersExist()
        fullRepo = project.gitconfig.fullRepoName
        try {
            checkFullRepoNameFormat()
            (org, repo) = project.gitconfig.fullRepoName.tokenize('/')
            log.info("Organization: " + org + " Repository: " + repo)
            currentPath = System.getProperty("user.dir")
            gitPath = currentPath + "/" + repo
            file = new File(gitPath)
        }catch (Exception e){
            log.error(e.printStackTrace(), e)
        }
    }

    def checkFullRepoNameFormat(){
        if(!project.gitconfig.fullRepoName.contains("/")){
            throw new Exception("The fullRepoName must have the following format: 'organization/repoName'")
        }
    }

    def parametersExist(){
        if(project.gitconfig.fullRepoName == null){
            throw new Exception('There is no fullRepoName defined, ' +
                    'please run this gradle task with -PfullRepoName=value')
        }
        if(project.gitconfig.userId == null){
            throw new Exception('There is no user id defined, ' +
                    'please run this gradle task with -PuserId=value')
        }
    }

    def execute(){
        commitToGit()
        pushToGit()
    }

    def commitToGit(){
        def userId = project.gitconfig.userId
        ["git", "commit", "-am",
                "'[${userId}] - Commit from Electric Commander'",
                "--author='${userId} <noreply@gap.com>'"].execute(null, file)
    }

    def pushToGit(){
        "git push".execute(null, file)
    }
}
