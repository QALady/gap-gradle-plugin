package com.gap.gradle.tasks

import org.junit.Test

import static org.mockito.Mockito.mock

import org.gradle.api.Project
import org.junit.Before

import static org.mockito.Mockito.verify
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.ExpectedException

import static org.junit.rules.ExpectedException.none
import com.gap.gradle.yum.YumClient
import com.gap.pipeline.ProdDeployParameterConfig

class PromoteRpmTaskTest {

    @Rule
    public ExpectedException exception = none()

    Project project
    def promoteRpmFromDevToProdTask
    def mockYumRepoClient

    @Before
    void setUp(){
        this.project = new ProjectBuilder().builder().build()
        project.extensions.create('prodDeploy', ProdDeployParameterConfig)

        project.prodDeploy.yumSourceUrl = "http://ks64.phx.gapinc.dev/gapSoftware/repoName/devel"
        project.prodDeploy.yumDestinationUrl = "http://ks64.phx.gapinc.com/gapSoftware/repoName/devel"
        project.prodDeploy.rpmName = 'rpmName-1234.rpm'
        project.prodDeploy.appVersion = "1234"

        this.mockYumRepoClient = mock(YumClient)
        promoteRpmFromDevToProdTask = new PromoteRpmTask(project, mockYumRepoClient)
    }

    @Test
    public void shouldPromoteRpmFromDevToProd(){
        promoteRpmFromDevToProdTask.execute()
        def expectedCopyToLocation = project.buildDir.path + '/tmp'
        verify(mockYumRepoClient).downloadRpm( "http://ks64.phx.gapinc.dev/gapSoftware/repoName/devel", "rpmName-1234.rpm", expectedCopyToLocation)
        verify(mockYumRepoClient).uploadRpm("rpmName-1234.rpm", expectedCopyToLocation, "http://ks64.phx.gapinc.com/gapSoftware/repoName/devel")
        verify(mockYumRepoClient).recreateYumRepo("http://ks64.phx.gapinc.com/gapSoftware/repoName/devel")
    }

    @Test
    public void validate_shouldVerifyThatRpmNameHasExtensionRpm(){
        project.prodDeploy.rpmName = 'thisisabadrpmname'

        exception.expect(IllegalArgumentException)
        exception.expectMessage("rpmConfig.rpmName thisisabadrpmname does not have .rpm extension")
        promoteRpmFromDevToProdTask.validate()
    }

    @Test
    public void validate_shouldVerifyThatRpmNameContainsAppVersion(){
        project.prodDeploy.rpmName = 'thisisabadrpmname.rpm'

        exception.expect(IllegalArgumentException)
        exception.expectMessage("rpmConfig.rpmName thisisabadrpmname.rpm does not contain app version 1234")
        promoteRpmFromDevToProdTask.validate()
    }
}
