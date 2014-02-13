package com.gap.gradle.plugins.git

import com.gap.gradle.git.GitClient
import com.gap.gradle.utils.ShellCommand
import groovy.mock.interceptor.MockFor
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.junit.Before
import org.junit.Test

class GitClientTest {

    GitClient client
    def mockShellCommand
    def mockFile
    def mockWriter
    private Log log = LogFactory.getLog(GitClientTest)

    @Before
    void setUp(){
        client = new GitClient("testUser", "testSHA", "test/repo")
        mockShellCommand = new MockFor(ShellCommand)
        mockFile = new MockFor(File)
        mockWriter = new MockFor(Writer)
    }

    @Test
    void checkoutShouldSucceed_whenFullRepoNameIsValid(){
        mockShellCommand.demand.execute(1){ command -> 0 }
        mockShellCommand.use {
            client.checkout()
        }
    }

    @Test
    void checkoutShouldThrowException_whenRepoNameIsInvalid(){
        mockShellCommand.demand.execute(1){ command -> -1 }
        mockShellCommand.use {
            client.checkout()
        }
    }

    @Test(expected = FileNotFoundException.class)
    void updateShouldThrowException_whenBerksfileDoesNotExist(){
        client.updateBerksfile()
    }

    @Test
    void commitShouldSucceed_whenParametersAreValid(){
        mockShellCommand.demand.execute(1..2){ command -> 0 }
        mockShellCommand.use {
            client.commitAndPush()
        }
    }

    @Test
    void commitShouldThrowException_whenParametersAreInvalid(){
        mockShellCommand.demand.execute(1..2){ command -> -1 }
        mockShellCommand.use {
            client.commitAndPush()
        }
    }
}
