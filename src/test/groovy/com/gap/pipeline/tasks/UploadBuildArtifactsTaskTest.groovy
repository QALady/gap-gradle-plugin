package com.gap.pipeline.tasks
import static junit.framework.Assert.*
import static org.junit.rules.ExpectedException.none

import com.gap.pipeline.exception.MissingParameterException
import junit.framework.Assert
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.rules.TemporaryFolder

class UploadBuildArtifactsTaskTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Rule
    public final ExpectedException exception = none()

    def project
    def uploadBuildArtifactsTask
    def artifactsDirPath

    @Before
    public void setUp() {
        project = ProjectBuilder.builder().withProjectDir(new File(temporaryFolder.root.path)).build()
        project.artifactCoordinates = "com.gap.test.myapp:${project.name}:5432"
        project.apply plugin: 'base' //doing this because uploadArchives is implemented in base as a rule
        project.apply plugin: 'gappipeline'
        project.ivy.password = "wrong password" //just to prevent unit tests from accidentally uploading to ivy
        project.task('uploadArchives', overwrite: true) << {}
        project.uploadArchives.repositories = {  }

        def artifactsDir = temporaryFolder.newFolder("build/artifacts")
        artifactsDir.mkdirs()
        artifactsDirPath = artifactsDir.path
        uploadBuildArtifactsTask = project.tasks.findByName('uploadBuildArtifacts')
    }


    @Test
    void shouldAddAllArtifactsInTheBuildDirectoryToArchivesConfiguration(){
        def artifacts = ["${artifactsDirPath}/deploy.json", "${artifactsDirPath}/report.txt"]
        artifacts.each{fileName -> "touch ${fileName}".execute()}

        uploadBuildArtifactsTask.execute()

        assertTrue(project.configurations.hasProperty('archives'))
        artifacts.each { fileName ->
            assertArchivesHasFile(fileName)
        }
    }

    @Test
    @Ignore  //TODO: to be fixed
    void shouldConfigureUploadArchivesTaskWithIvyCredentials () {
        project.ivy.url = "http://myurl"
        project.ivy.userName = "ivy_user"
        project.ivy.password = "ivy_password"
        uploadBuildArtifactsTask.execute()

        assertNotNull(project.uploadArchives)
        assertEquals("http://myurl", project.uploadArchives.repositories.ivy.url.toString())
        assertEquals("ivy_user", project.uploadArchives.repositories.ivy.credentials.username)
        assertEquals("ivy_password", project.uploadArchives.repositories.ivy.credentials.password)
    }

    @Test
    void shouldExecuteUploadArchivesTask () {
        def executed = false
        project.task('uploadArchives', overwrite: true) << { executed = true}
        project.uploadArchives.repositories = {}
        uploadBuildArtifactsTask.execute()
        assertTrue("Did not execute uploadArchives", executed)
    }

    @Test
    void shouldSetTheProjectGroupNameAndVersionFromArtifactCoordinates() {
        project.artifactCoordinates = "com.gap.test.myapp:${project.name}:5432"
        uploadBuildArtifactsTask.execute()
        assertEquals("com.gap.test.myapp", project.group)
        assertEquals("5432", project.version)
    }

    @Test
    void shouldThrowAnException_whenArtifactLocationNotProvided() {
        exception.expect(MissingParameterException)
        exception.expectMessage("Missing required parameter: 'artifactCoordinates")
        project.artifactCoordinates = null
        new UploadBuildArtifactsTask(project).validate()
    }

    @Test
    void shouldThrowAnException_whenArtifactLocationFormatIsIncorrect () {
        exception.expect(IllegalArgumentException)
        exception.expectMessage("The coordinates 'mylocation:iso' is of invalid format. The format should be <groupname>:<modulename>:<version>")
        project.artifactCoordinates = "mylocation:iso"
        new UploadBuildArtifactsTask(project).validate()
    }

    @Test
    void shouldThrowAnException_whenTheModuleNameInArtifactLocationDoesNotMatchTheProjectName() {
        exception.expect(IllegalArgumentException)
        exception.expectMessage("The module name in archiveLocation['iso'] does not match project name['12345']")
        project = ProjectBuilder.builder().withName("12345").build()
        project.artifactCoordinates = "com.gap.ref-app:iso:5432"

        new UploadBuildArtifactsTask(project).validate()
    }

    def assertArchivesHasFile(fileName){
        println  project.configurations.archives.allArtifacts.size()
        for(artifact in project.configurations.archives.allArtifacts){
            if (artifact.file.path.contains(fileName.toString())) return
        }
        Assert.fail("Cannot find file ${fileName} in the archives.")
    }
}
