package com.gap.pipeline.tasks

import static org.junit.rules.ExpectedException.none
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

import com.gap.pipeline.RpmConfig
import com.gap.pipeline.yum.YumClient
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class PromoteRpmTaskTest {

    @Rule
    public ExpectedException exception = none()

    Project project
    def promoteRpmFromDevToProdTask
    def mockYumRepoClient

    @Before
    void setUp(){
        this.project = new ProjectBuilder().builder().build()
        project.extensions.create('rpmConfig', RpmConfig)

        project.rpmConfig.repoUrl = "http://ks64.phx.gapinc.dev/gapSoftware/repoName/devel"
        project.rpmConfig.prodHostname = "ks64.phx.gapinc.com"
        project.rpmConfig.prodPath = "/mnt/repos/gapSoftware/repoName"
        project.rpmConfig.channel = "devel"
        project.rpmConfig.rpmName = 'rpmName-1234.rpm'
        project.rpmConfig.destination = "destination"
        project.rpmConfig.appVersion = "1234"

        this.mockYumRepoClient = mock(YumClient)
        promoteRpmFromDevToProdTask = new PromoteRpmTask(project, mockYumRepoClient)
    }

    @Test
    public void shouldPromoteRpmFromDevToProd(){
        promoteRpmFromDevToProdTask.execute()
        verify(mockYumRepoClient).downloadRpm( "http://ks64.phx.gapinc.dev/gapSoftware/repoName/devel", "rpmName-1234.rpm", "destination")
        verify(mockYumRepoClient).uploadRpm("rpmName-1234.rpm", "destination", "ks64.phx.gapinc.com", "/mnt/repos/gapSoftware/repoName", "devel")
        verify(mockYumRepoClient).recreateYumRepo("ks64.phx.gapinc.com", "/mnt/repos/gapSoftware/repoName", "devel")
    }

    @Test
    public void validate_shoudlVerifyThatRpmNameHasExtensionRpm(){
        project.rpmConfig.rpmName = 'thisisabadrpmname'

        exception.expect(IllegalArgumentException)
        exception.expectMessage("rpmConfig.rpmName thisisabadrpmname does not have .rpm extension")
        promoteRpmFromDevToProdTask.validate()
    }

    @Test
    public void validate_shouldVerifyThatRpmNameContainsAppVersion(){
        project.rpmConfig.rpmName = 'thisisabadrpmname.rpm'

        exception.expect(IllegalArgumentException)
        exception.expectMessage("rpmConfig.rpmName thisisabadrpmname.rpm does not contain app version 1234")
        promoteRpmFromDevToProdTask.validate()
    }
}
