package com.gap.pipeline.tasks

import com.gap.pipeline.exception.MissingParameterException
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.rules.TemporaryFolder

import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.CoreMatchers.notNullValue
import static org.junit.Assert.assertThat
import static org.junit.rules.ExpectedException.none

class DownloadArtifactsTaskTest {

    @Rule
    public TemporaryFolder destinationFolder = new TemporaryFolder()

    @Rule
    public TemporaryFolder dependencySourceFolder = new TemporaryFolder()

    @Rule
    public final ExpectedException exception = none()

    @Test
    public void configure_shouldAddWatchmenInternalConfigurationToProject(){
        Project project = ProjectBuilder.builder().build()
        project.metaClass.destination = 'dest'
        //project.artifactCoordinates="ivy.group:ivyname:7e184eca-0b87-11e4-8dba-00505625f614"
        project.metaClass.artifactCoordinates="ivy.group:ivyname:1234"
        project.metaClass.artifactConfiguration = "ivyConfig"

        def task = new DownloadArtifactsTask(project)
        project = task.configure()

        assertThat(project.configurations.findByName("_watchmenInternal"), is(notNullValue()))
    }

    @Test
    public void configure_shouldAddDependencyToConfiguration_whenRequiredParametersAreGiven(){
        Project project = ProjectBuilder.builder().build()
        project.metaClass.destination = 'dest'
        //project.artifactCoordinates="ivy.group:ivyname:7e184eca-0b87-11e4-8dba-00505625f614"
        project.metaClass.artifactCoordinates="ivy.group:ivyname:1234"
        project.metaClass.artifactConfiguration = "ivyConfig"

        def task = new DownloadArtifactsTask(project)
        project = task.configure()

        assertThat(project.configurations._watchmenInternal.dependencies.group, is(["ivy.group"]))
        assertThat(project.configurations._watchmenInternal.dependencies.name, is(["ivyname"]))
        //assertThat(project.configurations._watchmenInternal.dependencies.version, is(["7e184eca-0b87-11e4-8dba-00505625f614"]))
        assertThat(project.configurations._watchmenInternal.dependencies.version, is(["1234"]))
        assertThat(project.configurations._watchmenInternal.dependencies.configuration, is(["ivyConfig"]))
    }

    @Test
    public void configure_shouldThrowException_whenArtifactConfigurationIsNotProvided(){
        exception.expect(MissingParameterException)
        exception.expectMessage("Missing required parameter: 'artifactConfiguration'")
        Project project = ProjectBuilder.builder().build()
        project.metaClass.destination = 'dest'
        //project.artifactCoordinates="ivy.group:ivyname:7e184eca-0b87-11e4-8dba-00505625f614"
        project.metaClass.artifactCoordinates="ivy.group:ivyname:1234"
        def task = new DownloadArtifactsTask(project)
        task.configure()
    }

    @Test
    public void configure_shouldThrowException_whenArtifactCoordinatesAreNotProvided(){
        exception.expect(MissingParameterException)
        exception.expectMessage("Missing required parameter: 'artifactCoordinates'")

        Project project = ProjectBuilder.builder().build()
        project.metaClass.destination = 'dest'
        project.metaClass.artifactConfiguration = "ivyConfig"
        def task = new DownloadArtifactsTask(project)
        task.configure()
    }

    @Test
    public void configure_shouldThrowException_whenDestinationIsNotProvided(){
        exception.expect(MissingParameterException)
        exception.expectMessage("Missing required parameter: 'destination'")

        Project project = ProjectBuilder.builder().build()
        //project.artifactCoordinates="ivy.group:ivyname:7e184eca-0b87-11e4-8dba-00505625f614"
        project.metaClass.artifactCoordinates="ivy.group:ivyname:1234"
        project.metaClass.artifactConfiguration = "ivyConfig"
        def task = new DownloadArtifactsTask(project)
        task.configure()
    }

    @Test
    public void configure_shouldThrowException_whenArtifactCoordinatesAreNotValid(){
        exception.expect(IllegalArgumentException)
        exception.expectMessage("The coordinates 'ivy.group:ivyname' is of invalid format. The format should be <groupname>:<modulename>:<version>")

        Project project = ProjectBuilder.builder().build()
        project.metaClass.artifactCoordinates="ivy.group:ivyname"
        project.metaClass.artifactConfiguration = "ivyConfig"
        project.metaClass.destination = "dest"
        def task = new DownloadArtifactsTask(project)
        task.configure()
    }

    @Test
    public void execute_shouldCopyArtifactsFromWatchmenInternalConfigurationToDestination(){
        Project project = ProjectBuilder.builder().build()
        project.metaClass.destination = destinationFolder.root.path
        project.configurations.create('_watchmenInternal')

        //project = addNewFileToConfiguration(project, '_watchmenInternal', 'myFile-7e184eca-0b87-11e4-8dba-00505625f614.tmp')
        project = addNewFileToConfiguration(project, '_watchmenInternal', 'myFile.tmp')

        def task = new DownloadArtifactsTask(project)
        task.execute()
        File artifact = new File("${destinationFolder.root.path}/myFile.tmp");
        assertThat(artifact.exists(), is(true));
    }

    @Test
    public void execute_shouldRemoveIvyVersionFromFileName(){
        Project project = ProjectBuilder.builder().build()
        project.metaClass.destination = destinationFolder.root.path
        project.configurations.create('_watchmenInternal')

        //project = addNewFileToConfiguration(project, '_watchmenInternal', 'myFile-7e184eca-0b87-11e4-8dba-00505625f614.tmp')
        project = addNewFileToConfiguration(project, '_watchmenInternal', 'myFile-123.4.1234.tmp')

        def task = new DownloadArtifactsTask(project)
        task.execute()
        File artifact = new File("${destinationFolder.root.path}/myFile.tmp");
        assertThat(artifact.exists(), is(true));
    }

    private Project addNewFileToConfiguration(Project project, String configurationName, String fileName) {
        File tmpFile = dependencySourceFolder.newFile(fileName)
        FileCollection files = project.files(tmpFile)
        project.dependencies.add(configurationName, files)
        return project
    }
}
