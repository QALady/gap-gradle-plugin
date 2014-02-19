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

class PromoteRpmTaskTest {

    @Rule
    public ExpectedException exception = none()

    Project project
    def promoteRpmFromDevToProdTask
    def mockYumRepoClient

    @Before
    void setUp(){
        this.project = new ProjectBuilder().builder().build()
        project.extensions.create('rpm', RpmConfig)

        project.rpm.yumSourceUrl = "http://ks64.phx.gapinc.dev/gapSoftware/repoName/devel"
        project.rpm.yumDestinationUrl = "http://ks64.phx.gapinc.com/gapSoftware/repoName/devel"
        project.rpm.rpmName = 'rpmName-1234.rpm'
        project.rpm.appVersion = "1234"

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
        project.rpm.rpmName = 'thisisabadrpmname'

        exception.expect(IllegalArgumentException)
        exception.expectMessage("rpm.rpmName thisisabadrpmname does not have .rpm extension")
        promoteRpmFromDevToProdTask.validate()
    }

    @Test
    public void validate_shouldVerifyThatRpmNameContainsAppVersion(){
        project.rpm.rpmName = 'thisisabadrpmname.rpm'

        exception.expect(IllegalArgumentException)
        exception.expectMessage("rpm.rpmName thisisabadrpmname.rpm does not contain app version 1234")
        promoteRpmFromDevToProdTask.validate()
    }
}
