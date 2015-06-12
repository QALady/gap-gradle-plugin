package com.gap.pipeline.tasks

import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.utils.EnvironmentStub
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
 * Created by user on 6/12/15.
 */
class InsertResolvedVersionTaskTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private Project project
    private CommanderClient commanderClient
    private ShellCommand mockShellCommand

    private String projectDirPath
    private InsertResolvedVersionTask task

    @Before
    void setUp (){

        //init the mock for shell command
        mockShellCommand = mock(ShellCommand, Mockito.RETURNS_SMART_NULLS)
        commanderClient = new CommanderClient(mockShellCommand, new EnvironmentStub())

        //creating project with its own project folder and a ci folder and applying the gappipeline plugin
        project = new ProjectBuilder().builder().withProjectDir(new File("${tempFolder.root.path}")).withName("test").build()
        projectDirPath = project.projectDir.toString()
        def build_dir =   new File("${tempFolder.root.path}/build")
        build_dir.mkdirs()

        //creating the task to be tested, we are overriding the get configuration method
        task = new InsertResolvedVersionTask(project, commanderClient){
            @Override
            def get_configurations(){
                def configurations = []

                def configuration = [:]
                configuration['name'] = "archives"
                def dependency = [:]
                dependency['org'] = "com.gap.loki"
                dependency['name'] = "transformer"
                dependency['rev'] = "18622"
                configuration['dependencies'] = [dependency]
                configurations.push(configuration)

                configuration = [:]
                configuration['name'] = "cookbooks"
                dependency = [:]
                dependency['org'] = "com.gap.local_promo_pricing_transformer.infra"
                dependency['name'] = "ci"
                dependency['rev'] = "0.1.270.20150603151110"
                configuration['dependencies'] = [dependency]
                configurations.push(configuration)

                configuration = [:]
                configuration['name'] = "functionalTest"
                dependency = [:]
                dependency['org'] = "com.gap.loki"
                dependency['name'] = "transformer"
                dependency['rev'] = "18622"
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

        def newIviXmlText = new File("${tempFolder.root.path}/build/ivy.xml").text
        def expectedIviXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ivy-module version=\"2.0\" xmlns:m=\"http://ant.apache.org/ivy/maven\">\n" +
                "  <info organisation=\"com.gap.loki.transformer-iso\" module=\"ci\" revision=\"18054\" status=\"integration\" publication=\"20150604165109\">\n" +
                "    <description/>\n" +
                "  </info>\n" +
                "  <configurations>\n" +
                "    <conf name=\"archives\" visibility=\"public\" description=\"Configuration for archive artifacts.\"/>\n" +
                "    <conf name=\"cookbooks\" visibility=\"public\"/>\n" +
                "    <conf name=\"default\" visibility=\"public\" description=\"Configuration for default artifacts.\"/>\n" +
                "    <conf name=\"functionalTest\" visibility=\"public\"/>\n" +
                "    <conf name=\"pipeline\" visibility=\"public\"/>\n" +
                "  </configurations>\n" +
                "  <publications/>\n" +
                "  <dependencies>\n" +
                "    <dependency org=\"com.gap.loki\" name=\"transformer\" rev=\"18622\" conf=\"archives-&gt;archives\"/>\n" +
                "    <dependency org=\"com.gap.local_promo_pricing_transformer.infra\" name=\"ci\" rev=\"0.1.270.20150603151110\" conf=\"cookbooks-&gt;cookbooks\"/>\n" +
                "    <dependency org=\"com.gap.loki\" name=\"transformer\" rev=\"18622\" conf=\"functionalTest-&gt;archives\"/>\n" +
                "  </dependencies>\n" +
                "</ivy-module>\n" // ----> for some reason the writeLine method puts a "new line" character at the en of the last line

        assertEquals(expectedIviXml,newIviXmlText)
    }


    def create_ivy_file(){

        def ivyXml = new File("${tempFolder.root.path}/build/ivy.xml")
        ivyXml.createNewFile()

        FileUtils.writeStringToFile(ivyXml, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ivy-module version=\"2.0\" xmlns:m=\"http://ant.apache.org/ivy/maven\">\n" +
                "  <info organisation=\"com.gap.loki.transformer-iso\" module=\"ci\" revision=\"18054\" status=\"integration\" publication=\"20150604165109\">\n" +
                "    <description/>\n" +
                "  </info>\n" +
                "  <configurations>\n" +
                "    <conf name=\"archives\" visibility=\"public\" description=\"Configuration for archive artifacts.\"/>\n" +
                "    <conf name=\"cookbooks\" visibility=\"public\"/>\n" +
                "    <conf name=\"default\" visibility=\"public\" description=\"Configuration for default artifacts.\"/>\n" +
                "    <conf name=\"functionalTest\" visibility=\"public\"/>\n" +
                "    <conf name=\"pipeline\" visibility=\"public\"/>\n" +
                "  </configurations>\n" +
                "  <publications/>\n" +
                "  <dependencies>\n" +
                "    <dependency org=\"com.gap.loki\" name=\"transformer\" rev=\"+\" conf=\"archives-&gt;archives\"/>\n" +
                "    <dependency org=\"com.gap.local_promo_pricing_transformer.infra\" name=\"ci\" rev=\"+\" conf=\"cookbooks-&gt;cookbooks\"/>\n" +
                "    <dependency org=\"com.gap.loki\" name=\"transformer\" rev=\"+\" conf=\"functionalTest-&gt;archives\"/>\n" +
                "  </dependencies>\n" +
                "</ivy-module>")
    }
}