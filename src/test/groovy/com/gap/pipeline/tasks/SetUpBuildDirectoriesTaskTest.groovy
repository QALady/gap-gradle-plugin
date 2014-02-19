package com.gap.pipeline.tasks

import static junit.framework.Assert.assertTrue

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SetUpBuildDirectoriesTaskTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    def project
    def setupBuildDirectoriesTask

    @Before
    public void setUp() {
        project = ProjectBuilder.builder().withProjectDir(new File(temporaryFolder.root.path)).build()
        setupBuildDirectoriesTask = new SetUpBuildDirectoriesTask(project)
    }

    @Test
    public void shouldCreateBuildDirectories(){
        project.buildDir = "${temporaryFolder.root.path}/build"
        setupBuildDirectoriesTask.execute()
        File buildArtifacts = new File("${temporaryFolder.root.path}/build/artifacts");
        assertTrue( buildArtifacts.exists());
        File buildReports = new File("${temporaryFolder.root.path}/build/reports");
        assertTrue( buildReports.exists());
    }

    @Test
    public void shouldNotThrowException_whenDirectoryAlreadyExists(){
        def project = ProjectBuilder.builder().build()
        project.buildDir = temporaryFolder.root.path
        File file = new File("${project.buildDir}/artifacts");
        file.mkdirs()
        File reportsFile = new File("${project.buildDir}/reports");
        reportsFile.mkdirs()

        setupBuildDirectoriesTask.execute()
    }

}
