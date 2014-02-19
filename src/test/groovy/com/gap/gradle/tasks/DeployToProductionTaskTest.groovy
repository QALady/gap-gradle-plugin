package com.gap.gradle.tasks
import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.containsString
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail

import com.gap.gradle.exceptions.DeployToProductionException;
import com.gap.gradle.utils.ShellCommand
import com.gap.gradle.utils.ShellCommandException

import groovy.mock.interceptor.MockFor

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class DeployToProductionTaskTest {

    Project project
    Task task

    @Before
    void setUp() {
        project = ProjectBuilder.builder().build();
        project.apply plugin: 'gapproddeploy'
        task = project.tasks.findByName('deployToProduction')
    }

    @Test
    void shouldDeployToNodes() {
        def commandMock = new MockFor(ShellCommand)
        def executedCommands = []
        project.prodDeploy.nodes = [ 'node1.phx.gapinc.com', 'node2.phx.gapinc.com', 'node3.phx.gapinc.com' ]
        commandMock.demand.execute(3) { executedCommands << it }
        commandMock.use {
            task.execute()
        }
        assertThat(executedCommands, contains(
            "ssh node1.phx.gapinc.com 'chef-client'",
            "ssh node2.phx.gapinc.com 'chef-client'",
            "ssh node3.phx.gapinc.com 'chef-client'"
        ))
    }

    @Test
    void shouldFail_ifAnyDeployFails() {
        try {
            def commandMock = new MockFor(ShellCommand)
            project.prodDeploy.nodes = [ 'node1.phx.gapinc.com', 'busted.phx.gapinc.com', 'node3.phx.gapinc.com' ]
            commandMock.demand.execute(3) { command ->
                if (command.contains("busted")) {
                    throw new ShellCommandException("Oops! You bwoke it :(")
                }
            }
            commandMock.use {
                task.execute()
            }
            fail("No exception thrown!")
        } catch (Exception exception) {
            assertCause(exception, DeployToProductionException, "Failed to deploy on node 'busted.phx.gapinc.com'")
        }
    }

    static def assertCause(Throwable throwable, Class<?> type, String message) {
        assertThat(extractCause(throwable, type).message, containsString(message))
    }

    static def extractCause(Throwable throwable, Class<?> type) {
        Throwable originalException = throwable
        Throwable extractedException = throwable
        while (!(type.isInstance(extractedException))) {
            if (extractedException.cause == null) {
                throw new AssertionError("Expected exception '${type.name}' but got '${throwable.class.name}'", originalException)
            }
            extractedException = extractedException.cause
        }
        extractedException
    }
}
