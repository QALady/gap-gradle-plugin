package com.gap.gradle.plugins.git

import com.gap.gradle.utils.ShellCommand
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Created by ccaceres on 2/10/14.
 */
class GitClient {

    def userId
    def shaId
    def fullRepoName
    def gitPath
    def location
    private Log log = LogFactory.getLog(GitClient)

    GitClient(userId, shaId, fullRepoName){
        this.userId = userId
        this.shaId = shaId
        this.fullRepoName = fullRepoName
        def currentPath = System.getProperty("user.dir")
        def repo = this.fullRepoName.tokenize('/')[1]
        gitPath = currentPath + "/" + repo
        location = new File(gitPath)
    }

    def checkout(){
        new ShellCommand().execute('git clone git@github.gapinc.dev:'
                + fullRepoName + '.git', null);
    }

    def commitAndPush(){
        new ShellCommand().execute(["git", "commit", "-am",
                "'[${userId}] - Commit from Electric Commander'",
                "--author='${userId} <noreply@gap.com>'"], location)
        new ShellCommand().execute("git push", location)
    }

    def updateBerksfile() throws FileNotFoundException{
        def (org, cookbook) = fullRepoName.tokenize('/')
        log.debug("Cookbook: " + cookbook + " Org: " + org)
        File berksfile = new File(cookbook + '//Berksfile.prod')
        def fileContents = berksfile.getText('UTF-8')
        def replacedFileContent = fileContents.replaceAll(/ref: '.*'/, "ref: '"
                + shaId + "'")
        def writer = berksfile.newWriter()
        writer.write(replacedFileContent)
        writer.close()
    }
}
