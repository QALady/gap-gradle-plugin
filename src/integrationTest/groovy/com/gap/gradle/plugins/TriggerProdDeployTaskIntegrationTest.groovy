package com.gap.gradle.plugins

import groovy.json.JsonBuilder

import org.apache.commons.io.FileUtils
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Ignore;
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.gradle.api.Project

import com.gap.gradle.ProdDeployConfig

@Ignore
class TriggerProdDeployTaskIntegrationTest {

    Project project

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    void shouldPromoteChefObjectsToServerUsingJenkinsPipeline() {
        project = ProjectBuilder.builder().build()
        createProdDeployConfigFile()
        project.apply plugin: 'gapproddeploy'
        project.jenkins.knifeServerUrl = "http://jenkins01.phx.gapinc.dev:8080"
        project.jenkins.knifeUser = "kr8s8k9"
        project.jenkins.knifeJobName = "TagProdReady"
        project.jenkins.knifeAuthToken = "4661bb66b1f850bdff9c3ce5f5daca65"

        def triggerProdDeployTask = project.tasks.findByName('triggerProdDeploy')

        triggerProdDeployTask.execute()
    }

    void createProdDeployConfigFile(){
        ProdDeployConfig config = new ProdDeployConfig()
        config.sha1IdList = ["1234", "4567"]

        def jsonBuilder = new JsonBuilder(config)
        def fileWriter = new FileWriter("${ProdDeployConfig.PARAMJSON}")
        jsonBuilder.writeTo(fileWriter)
        fileWriter.close()
    }
}
