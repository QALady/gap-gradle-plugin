package com.gap.gradle.tasks

import org.apache.commons.io.FileUtils
import org.junit.Rule
import org.junit.rules.TemporaryFolder

import static helpers.CustomMatchers.sameString
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import com.gap.gradle.utils.ShellCommand


class UploadAthenaBuildRpmToRepoTest {
    private UploadAthenaBuildRpmToRepo myUploadAthena
    private Project project
    private def mockShellCommand

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    void setup() {
        project = ProjectBuilder.builder().build()
        project.athenaLocalRpmBase = tempFolder.root.path
        mockShellCommand = mock(ShellCommand)
        myUploadAthena = new UploadAthenaBuildRpmToRepo(project, mockShellCommand)

    }

    @Test
    void testExecuteTask() {
        def randomPropFile = tempFolder.newFile("gap-pos-database-14.041.info")
        FileUtils.writeStringToFile(randomPropFile, "unexistingFile.rpm")
        def scpFile = tempFolder.newFile("s2c_gap-pos-14.041.info")
        FileUtils.writeStringToFile(scpFile, "sc2_unexistingFile.rpm")
        myUploadAthena.execute()

        verify(mockShellCommand).execute(["ssh", "cibuild@repo1.phx.gapinc.dev", "ls -1 /mnt/repos/stores/stores-custom-iss/s2c_gap-pos*rpm | wc -l"])


        verify(mockShellCommand).execute(sameString("scp ${tempFolder.root.path}/sc2_unexistingFile.rpm cibuild@repo1.phx.gapinc.dev:/mnt/repos/stores/stores-custom-iss/sc2_unexistingFile.rpm"))
        verify(mockShellCommand).execute(sameString("scp ${tempFolder.root.path}/sc2_unexistingFile.rpm cibuild@repo1.phx.gapinc.dev:/mnt/repos/stores/stores-custom-register/sc2_unexistingFile.rpm"))
        verify(mockShellCommand).execute(sameString("scp ${tempFolder.root.path}/unexistingFile.rpm cibuild@repo1.phx.gapinc.dev:/mnt/repos/stores/stores-custom-iss/unexistingFile.rpm"))
        verify(mockShellCommand).execute(["ssh", "cibuild@repo1.phx.gapinc.dev", "cd /mnt/repos/stores/stores-custom-iss; sudo /usr/bin/createrepo ."])
        verify(mockShellCommand).execute(["ssh", "cibuild@repo1.phx.gapinc.dev", "cd /mnt/repos/stores/stores-custom-register; sudo /usr/bin/createrepo ."])

    }
}