package com.gap.gradle.tasks

import static org.junit.rules.ExpectedException.none
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

import com.gap.gradle.yum.YumClient
import com.gap.pipeline.RpmConfig

import com.gap.pipeline.ProdDeployParameterConfig

class PromoteRpmsTaskTest {

    @Rule
    public ExpectedException exception = none()

    Project project
    def promoteRpmFromDevToProdTask
    def mockYumRepoClient

    @Before
    void setUp(){
        this.project = new ProjectBuilder().builder().build()
        project.extensions.create('rpm', RpmConfig)
        project.extensions.create('prodDeploy', ProdDeployParameterConfig)

        project.rpm.yumSourceUrl = "http://ks64.phx.gapinc.dev/gapSoftware/repoName/devel"
        project.rpm.yumDestinationUrl = "http://ks64.phx.gapinc.com/gapSoftware/repoName/devel"
        project.rpm.rpmNames = ['rpmNames-1234.rpm', 'anotherRpm-1234.rpm', 'yetAnother-1234.rpm']
        project.rpm.appVersion = "1234"

        this.mockYumRepoClient = mock(YumClient)
        promoteRpmFromDevToProdTask = new PromoteRpmsTask(project, mockYumRepoClient)
    }

    @Test
    public void shouldPromoteRpmsFromDevToProd(){
        promoteRpmFromDevToProdTask.execute()
        def expectedCopyToLocation = project.buildDir.path + '/tmp'
        verify(mockYumRepoClient).downloadRpm( "http://ks64.phx.gapinc.dev/gapSoftware/repoName/devel", "rpmNames-1234.rpm", expectedCopyToLocation)
        verify(mockYumRepoClient).downloadRpm( "http://ks64.phx.gapinc.dev/gapSoftware/repoName/devel", "anotherRpm-1234.rpm", expectedCopyToLocation)
        verify(mockYumRepoClient).downloadRpm( "http://ks64.phx.gapinc.dev/gapSoftware/repoName/devel", "yetAnother-1234.rpm", expectedCopyToLocation)
        verify(mockYumRepoClient).uploadRpm("rpmNames-1234.rpm", expectedCopyToLocation, "http://ks64.phx.gapinc.com/gapSoftware/repoName/devel")
        verify(mockYumRepoClient).uploadRpm("anotherRpm-1234.rpm", expectedCopyToLocation, "http://ks64.phx.gapinc.com/gapSoftware/repoName/devel")
        verify(mockYumRepoClient).uploadRpm("yetAnother-1234.rpm", expectedCopyToLocation, "http://ks64.phx.gapinc.com/gapSoftware/repoName/devel")
        verify(mockYumRepoClient).recreateYumRepo("http://ks64.phx.gapinc.com/gapSoftware/repoName/devel")
    }

    @Test
    public void validate_shouldVerifyThatRpmNameHasExtensionRpm(){
        project.rpm.rpmNames = ['thisisabadrpmname']

        exception.expect(IllegalArgumentException)
        exception.expectMessage("rpm.rpmNames thisisabadrpmname does not have .rpm extension")
        promoteRpmFromDevToProdTask.validate()
    }

    @Test
    public void validate_shouldVerifyThatEachRpmNameContainsAppVersion(){
        project.rpm.rpmNames = ['rpmNames-1234.rpm', 'thisisabadrpmname.rpm']

        exception.expect(IllegalArgumentException)
        exception.expectMessage("rpm.rpmNames thisisabadrpmname.rpm does not contain app version 1234")
        promoteRpmFromDevToProdTask.validate()
    }
}
