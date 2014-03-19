package com.gap.pipeline.ec

import org.apache.commons.io.FileUtils
import org.junit.Assume
import org.junit.Before

import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.verify
import static org.testng.Assert.assertTrue

import com.gap.pipeline.utils.ShellCommand
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CommanderClientIntegrationTest {

    def pipeline = true

    CommanderClient commander = null

    @Before
    def void beforeTests(){
        Assume.assumeTrue(pipeline)
        commander = new CommanderClient()
    }

    @Test
    public void addLink_shouldInvokeEcToolToCreateLinks(){
        def jobId = commander.getJobId()

        def jobDirectory = commander.currentJobDir
        def artifactsDir = "${jobDirectory}/artifacts"
        FileUtils.forceMkdir(artifactsDir)
        createTextFile("${artifactsDir}/integrationTest.log")
        commander.addLink("integrationTest.log", jobId)
    }

    private void createTextFile(filePath){
        String content = "This is the content to write into file";

        File file = new File(filePath)

        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile()
        }

        FileWriter fw = new FileWriter(file.getAbsoluteFile())
        BufferedWriter bw = new BufferedWriter(fw)
        bw.write(content)
        bw.close()
    }


}
