package com.gap.pipeline

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.Test
import org.gradle.testfixtures.ProjectBuilder

import static org.testng.Assert.assertTrue
import com.gap.pipeline.utils.ShellCommand
import static org.junit.Assert.assertTrue
import org.junit.After

class PromoteRpmIntegrationTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Test
    public void shouldDownloadRefAppRpmToTempFolder(){
        def project = ProjectBuilder.builder().build()
        project.apply plugin: 'gappipeline'
        project.rpmConfig.repoUrl = "http://ks64.phx.gapinc.dev/gapSoftware/watchmen/devel/"
        project.rpmConfig.prodHostname = "ks64.phx.gapinc.dev"
        project.rpmConfig.prodPath = "/mnt/repos/gapSoftware/watchmen"
        project.rpmConfig.channel = "testing"
        project.rpmConfig.rpmName = "ref-app-1161-1.noarch.rpm"
        project.rpmConfig.destination = temporaryFolder.root.path
        project.rpmConfig.appVersion = '1161'

        project.tasks.findByName('promoteRpm').execute()

        def expectedFile = new File("${temporaryFolder.root.path}/ref-app-1161-1.noarch.rpm")
        assertTrue(expectedFile.exists())

        def output = new ShellCommand().execute("curl http://ks64.phx.gapinc.dev/gapSoftware/watchmen/testing/")
        assertTrue(output.contains(project.rpmConfig.rpmName))
    }

    @After
    public void tearDown(){
        new ShellCommand().execute(["ssh", "ks64.phx.gapinc.dev", "rm /mnt/repos/gapSoftware/watchmen/testing/ref-app-1161-1.noarch.rpm"])
    }
}

