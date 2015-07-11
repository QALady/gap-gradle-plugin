package com.gap.pipeline.tasks

import aQute.libg.command.Command
import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.ec.CommanderClient
import groovy.mock.interceptor.MockFor
import org.apache.commons.io.FileUtils
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.gradle.api.Project
import org.mockito.Mockito

import static org.junit.Assert.assertEquals
import static org.mockito.Mockito.mock

/**
 * Created by Cl3s1l0 on 06-08-15.
 */
class InsertResolvedVersionTaskTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private Project project
    private ShellCommand mockShellCommand
    private CommanderClient mockCommanderClient
    private String projectDirPath
    private InsertResolvedVersionTask task

    @Before
    void setUp (){

        //init the mock for shell command
        mockShellCommand = mock(ShellCommand, Mockito.RETURNS_SMART_NULLS)
        mockCommanderClient = new CommanderClient(mockShellCommand);

        //creating project with its own project folder and a ci folder and applying the gappipeline plugin
        project = new ProjectBuilder().builder().withProjectDir(new File("${tempFolder.root.path}")).withName("test").build()
        projectDirPath = project.projectDir.toString()

        project.buildDir  = tempFolder.root

        //creating the task to be tested, we are overriding the get configuration method
        task = new InsertResolvedVersionTask(project,mockShellCommand,mockCommanderClient){
            //@Override
            def get_configurations(){
                def configurations = []

                def configuration = [:]
                configuration['name'] = "archives"
                def dependency = [:]
                dependency['org'] = "com.gap.watchmen.diamondDependency"
                dependency['name'] = "diamondDependencyA"
                dependency['rev'] = "177"
                configuration['dependencies'] = [dependency]
                configurations.push(configuration)

                return configurations
            }
        }

        create_ivy_file()
        task.execute()
    }

    @Test
    public void ivy_file_successfully_modified() throws Exception {

        def newIviXmlText = new File("${project.buildDir.path}/ivy.xml").text
        def expectedIviXml = this.getClass().getClassLoader().getResource('expected-ivy.xml' ).text

        assertEquals(expectedIviXml,newIviXmlText)
    }


    def create_ivy_file(){
        def ivyXml = new File("${project.buildDir.path}/ivy.xml")
        ivyXml.createNewFile()

        def templateIvy = this.getClass().getClassLoader().getResource('template-ivy.xml').text
        FileUtils.writeStringToFile(ivyXml, templateIvy)
    }
}