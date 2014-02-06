package com.gap.gradle.plugins.git

import com.gap.gradle.utils.ShellCommand
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

/**
 * Created by ccaceres on 2/4/14.
 */
class GitCheckoutTask {

    Project project
    private Log log = LogFactory.getLog(GitCheckoutTask)

    GitCheckoutTask(Project project){
        this.project = project
        parametersExist()
    }

    def parametersExist(){
        if(project.gitconfig.fullRepoName == null){
            throw new Exception('There is no fullRepoName defined')
        }
    }

    def execute(){
        checkoutGit()
    }

    def checkoutGit(){
        def status = new ShellCommand().execute('git clone git@github.gapinc.dev:'
                + project.gitconfig.fullRepoName + '.git');
        return status
    }
}
