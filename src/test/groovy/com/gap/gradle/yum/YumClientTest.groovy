package com.gap.gradle.yum

import org.junit.Test
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import com.gap.gradle.utils.ShellCommand
import static helpers.CustomMatchers.sameString

class YumClientTest {
    @Test
    public void shouldDownloadRpmFromYumRepo(){
        def repoUrl = "http://yumrepo.dev/gapSoftware/repoName/devel"
        def rpmName = "name"
        def copyToLocation = 'tmp'
        def mockShellCommand = mock(ShellCommand)
        def yum = new YumClient(mockShellCommand)

        yum.downloadRpm(repoUrl, rpmName, copyToLocation)

        verify(mockShellCommand).execute(sameString("curl -o tmp/name --create-dirs --fail http://yumrepo.dev/gapSoftware/repoName/devel/name"))
    }

    @Test
    public void shouldUploadRpmToProdYumRepo(){
        def rpmName = "name"
        def destination = "destination/dir"
        def yumDestinationUrl = "http://yumrepo.prod/gapSoftware/repoName/devel"
        def mockShellCommand = mock(ShellCommand)
        def yum = new YumClient(mockShellCommand)

        yum.uploadRpm(rpmName, destination, yumDestinationUrl)

        verify(mockShellCommand).execute(sameString("scp destination/dir/name yumrepo.prod:/mnt/repos/gapSoftware/repoName/devel/name"))
    }

    @Test
    public void shouldRecreateProdYumRepo(){
        def yumDestinationUrl = "http://yumrepo.prod/gapSoftware/repoName/devel"
        def mockShellCommand = mock(ShellCommand)
        def yum = new YumClient(mockShellCommand)

        yum.recreateYumRepo(yumDestinationUrl)

        verify(mockShellCommand).execute(["ssh", "yumrepo.prod", "sudo createrepo --database --update /mnt/repos/gapSoftware/repoName/devel"])
    }
}
