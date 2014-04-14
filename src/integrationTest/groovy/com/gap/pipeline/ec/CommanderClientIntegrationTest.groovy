package com.gap.pipeline.ec

import com.gap.pipeline.utils.Environment
import org.junit.Assume
import org.junit.Before
import org.junit.Test

import static junit.framework.Assert.assertEquals
import static junit.framework.Assert.assertTrue

class CommanderClientIntegrationTest {

    def pipeline = true

    CommanderClient commander = null
    Environment environment = null

    @Before
    def void beforeTests(){
        environment = new Environment()
        Assume.assumeNotNull(environment.getValue('COMMANDER_HOME')) //this ensures that the tests run only in the pipeline and not locally
        commander = new CommanderClient()
    }

    @Test
    public void shouldReturnCurrentJobID(){
        assertTrue(commander.getJobId().length() > 0)
    }

    @Test
    public void shouldReturnUserId() {
        assertTrue(commander.getUserId().length() > 0)
    }

    @Test
    public void shouldReturnUserName(){
        assertTrue(commander.getUserName().length() > 0)
    }

    @Test
    public void shouldReturnStartTimeOfJob(){
        assertTrue(commander.getStartTime() != null)
    }

    @Test
    public void shouldReturnCurrentDirectoryAsJobWorkingDirectory(){
        def ecJobName = "Watchmen Framework-Gap Gradle Plugin"
        def jobDir = "/mnt/electriccommander2/workspace/${ecJobName}-${commander.getJobId()}"
        assertEquals(jobDir, commander.getCurrentJobDir())
    }
}
