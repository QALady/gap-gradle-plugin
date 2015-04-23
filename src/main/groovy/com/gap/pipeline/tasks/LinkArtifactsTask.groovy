package com.gap.pipeline.tasks

import com.gap.gradle.plugins.mobile.CommandRunner
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import groovy.io.FileType
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

@RequiredParameters([
    @Require(parameter = "artifactLocation", description = "Location of the artifacts which needs to be ")
])

class LinkArtifactsTask extends WatchmenTask {

    private static final String DIR_HTML = "dir.html"
    private static final Log log = LogFactory.getLog(LinkArtifactsTask)

    CommanderClient commanderClient
    CommandRunner commandRunner
    String artifactsSource
    String artifactsDestination

    LinkArtifactsTask(Project project, CommanderClient commanderClient) {
        super(project)
        this.commanderClient = commanderClient
        this.commandRunner = new CommandRunner(project)
        this.artifactsSource = project.artifactLocation
        this.artifactsDestination = "${commanderClient.currentJobDir}/artifacts"
    }

    void copyArtifacts() {
        commandRunner.run("cp", "-R", artifactsSource, artifactsDestination)
    }

    void createHtmlIndex() {
        def dir = new File(artifactsDestination)

        runTreeCommand(dir)

        dir.eachFileRecurse(FileType.DIRECTORIES) { subDir ->
            runTreeCommand(subDir)
        }
    }

    void runTreeCommand(File dir) {
        commandRunner.run(dir, "tree", "--dirsfirst", "-CF", "-o", DIR_HTML, "-H", ".", "-L", "1", "-T", dir, "-I", DIR_HTML)

        cleanupHtmlIndex(dir)
    }

    void cleanupHtmlIndex(File dir) {
        File originalFile = new File(dir, DIR_HTML)
        Document htmlContent = Jsoup.parse(originalFile, "UTF-8")

        htmlContent.select("hr").remove()
        htmlContent.select("p.version").remove()

        originalFile.write(htmlContent.outerHtml())
    }

    void linkArtifacts() {
        commanderClient.addLink("${artifactsDestination}/${DIR_HTML}".toString(), commanderClient.getJobId())
    }

    def execute() {
        log.info("Executing task linkArtifacts...")
        copyArtifacts()
        createHtmlIndex()
        linkArtifacts()
    }
}
