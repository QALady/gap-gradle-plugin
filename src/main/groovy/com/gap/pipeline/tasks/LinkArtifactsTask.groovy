package com.gap.pipeline.tasks

import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import groovy.io.FileType
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

@RequiredParameters([
    @Require(parameter = "artifactLocation", description = "Location of the artifacts which needs to be ")
])

class LinkArtifactsTask extends WatchmenTask {

    private static final String DIR_HTML = "dir.html"

    private Project project
    CommanderClient commanderClient
    def artifactLocation
    String commanderArtifactLocation
    def log = LogFactory.getLog(LinkArtifactsTask)

    LinkArtifactsTask(Project project) {
        super(project)
        this.project = project
        this.commanderClient = new CommanderClient()
        this.artifactLocation = project.artifactLocation
        this.commanderArtifactLocation = "${commanderClient.currentJobDir}/artifacts"
    }

    private void copyArtifacts() {
        ShellCommand shellCommandExecutor = new ShellCommand()
        shellCommandExecutor.execute("cp -R ${artifactLocation} ${commanderArtifactLocation}")
    }

    private void createHtmlIndex() {
        def dir = new File(commanderArtifactLocation)

        runTreeCommand(dir)

        dir.eachFileRecurse(FileType.DIRECTORIES) { subDir ->
            runTreeCommand(subDir)
        }
    }

    private void runTreeCommand(File dir) {
        ShellCommand shellCmdExecutor = new ShellCommand(dir)
        shellCmdExecutor.execute("tree --dirsfirst -CFo ${DIR_HTML} -H . -L 1 -I ${DIR_HTML} -T ${dir.absolutePath}")

        cleanupHtmlIndex(dir)
    }

    private void cleanupHtmlIndex(File dir) {
        File originalFile = new File(dir, DIR_HTML)
        Document htmlContent = Jsoup.parse(originalFile, "UTF-8")

        htmlContent.select("hr").remove()
        htmlContent.select("p.version").remove()

        originalFile.write(htmlContent.outerHtml())
    }

    private void linkArtifacts() {
        commanderClient.addLink("${commanderArtifactLocation}/${DIR_HTML}", commanderClient.getJobId())
    }

    def execute() {
        log.info("Executing task linkArtifacts...")
        copyArtifacts()
        createHtmlIndex()
        linkArtifacts()
    }
}
