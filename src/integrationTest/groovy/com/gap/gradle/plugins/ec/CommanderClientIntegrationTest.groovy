package com.gap.gradle.plugins.ec

import static junit.framework.Assert.assertTrue
import static org.hamcrest.CoreMatchers.containsString
import static org.junit.Assert.assertThat
import static org.junit.Assume.assumeTrue

import org.junit.Before
import org.junit.Test

import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.utils.Environment
import com.gap.gradle.plugins.helpers.Util

class CommanderClientIntegrationTest {

    def pipeline = true

    CommanderClient commander = null
    Environment environment = null

    @Before
    def void beforeTests(){
        environment = new Environment()
        assumeTrue(Util.isRunningInPipeline()) //this ensures that the tests run only in the pipeline and not locally
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
    public void shouldReturnJobWorkingDirectory(){
        assertThat(commander.getCurrentJobDir(), containsString(commander.getJobId()))
    }
}
