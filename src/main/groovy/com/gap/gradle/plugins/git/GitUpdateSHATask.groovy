package com.gap.gradle.plugins.git

import com.gap.gradle.utils.ShellCommand
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

import java.util.regex.Matcher

/**
 * Created by ccaceres on 2/5/14.
 */
class GitUpdateSHATask {

    Project project
    File file
    BufferedWriter writer
    private Log log = LogFactory.getLog(GitUpdateSHATask)

    GitUpdateSHATask(Project project){
        this.project = project
        parametersExist()
    }

    def execute(){
        updateSHA()
    }

    def updateSHA(){
        try{
            def (org, cookbook) = project.gitconfig.fullRepoName.tokenize('/')
            log.info("Organization: " + org + " Cookbook: " + cookbook)
            file = new File(cookbook + '/Berksfile.prod')
            def fileContents = file.getText('UTF-8')
            def replacedFileContent = fileContents.replaceAll(/ref: '.*'/, "ref: '"
                    + project.gitconfig.shaId + "'")
            writer = file.newWriter()
            writer.write(replacedFileContent)
            writer.close()
        }
        catch (FileNotFoundException e){
            log.error(e.printStackTrace())
        }
    }

    def parametersExist(){
        if(project.gitconfig.fullRepoName == null){
            throw new Exception('There is no fullRepoName defined')
        }
        if(project.gitconfig.shaId == null){
            throw new Exception('There is no SHA Id defined')
        }
    }

}
