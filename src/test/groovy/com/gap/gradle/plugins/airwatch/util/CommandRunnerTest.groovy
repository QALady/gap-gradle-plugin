package com.gap.gradle.plugins.airwatch.util

import com.gap.gradle.plugins.mobile.CommandRunner
import org.gradle.api.Project
import org.gradle.process.ExecResult
import org.junit.Before
import org.junit.Test
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import static org.junit.Assert.assertEquals
import static org.mockito.Matchers.any
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

public class CommandRunnerTest {

    private File workingDirParam
    private String[] commandLineParams
    private OutputStream standardOutput
    private CommandRunner commandRunner

    @Before
    public void setUp() throws Exception {
        def project = mock(Project)

        when(project.exec(any(Closure))).thenAnswer(new Answer<ExecResult>() {
            @Override
            ExecResult answer(InvocationOnMock invocation) throws Throwable {
                invocation.arguments[0].delegate = this
                invocation.arguments[0].call()

                standardOutput.write(" hello\n world\n".bytes)

                return null
            }
        })

        commandRunner = new CommandRunner(project)
    }

    @Test
    public void shouldCaptureExecOutput() throws Exception {
        def output = commandRunner.run("foo")

        assertEquals(new File("."), workingDirParam)
        assertEquals("foo", commandLineParams[0])
        assertEquals("hello\n world", output)
    }

    @Test
    public void shouldAcceptOverridingWorkingDir() throws Exception {
        def baseDir = new File("/path/to/some/dir")

        def output = commandRunner.run(baseDir, "bar")

        assertEquals(baseDir, workingDirParam)
        assertEquals("bar", commandLineParams[0])
        assertEquals("hello\n world", output)
    }

    private void commandLine(Object[] args) {
        commandLineParams = args
    }

    private void workingDir(File dir) {
        workingDirParam = dir
    }
}
