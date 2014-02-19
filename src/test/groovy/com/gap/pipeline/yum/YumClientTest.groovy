package com.gap.pipeline.yum

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

import com.gap.pipeline.utils.ShellCommand
import org.junit.Test

import static matchers.CustomMatchers.sameString

class YumClientTest {


    @Test
    public void shouldDownloadRpmFromYumRepo(){
        def repoUrl = "http://yumrepo.dev/gapSoftware/repoName/devel"
        def rpmName = "name"
        def destination = "destination/dir"


        def mockShellCommand = mock(ShellCommand)
        def yum = new YumClient(mockShellCommand)
        yum.downloadRpm(repoUrl, rpmName, destination)
        verify(mockShellCommand).execute(matchers.CustomMatchers.sameString("curl -o destination/dir/name --create-dirs http://yumrepo.dev/gapSoftware/repoName/devel/name"))
    }

    @Test
    public void shouldUploadRpmToProdYumRepo(){
        def rpmName = "name"
        def rpmLocation = "destination/dir"
        def prodHostname = "yumrepo.prod"
        def prodPath = "/mnt/repos/gapSoftware/repoName"
        def channel = "devel"

        def mockShellCommand = mock(ShellCommand)
        def yum = new YumClient(mockShellCommand)
        yum.uploadRpm(rpmName, rpmLocation, prodHostname, prodPath, channel)
        verify(mockShellCommand).execute(matchers.CustomMatchers.sameString("scp destination/dir/name yumrepo.prod:/mnt/repos/gapSoftware/repoName/devel/name"))
    }

    @Test
    public void shouldRecreateProdYumRepo(){
        def prodHostname = "yumrepo.prod"
        def prodPath = "/mnt/repos/gapSoftware/repoName"
        def channel = "devel"

        def mockShellCommand = mock(ShellCommand)
        def yum = new YumClient(mockShellCommand)
        yum.recreateYumRepo(prodHostname, prodPath, channel)
        verify(mockShellCommand).execute(["ssh", "yumrepo.prod", "sudo createrepo --database --update /mnt/repos/gapSoftware/repoName/devel"])
    }
}
