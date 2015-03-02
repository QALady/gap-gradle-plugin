package com.gap.gradle.airwatch.util
import org.gradle.api.Project
import org.gradle.process.ExecResult
import org.junit.Test
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import static org.junit.Assert.assertEquals
import static org.mockito.Matchers.any
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

public class CommandRunnerTest {

    private String[] commandLineParams
    private OutputStream standardOutput

    @Test
    public void shouldCaptureExecOutput() throws Exception {
        def project = mock(Project)

        when(project.exec(any(Closure))).thenAnswer(new Answer<ExecResult>() {
            @Override
            ExecResult answer(InvocationOnMock invocation) throws Throwable {
                invocation.arguments[0].delegate = this
                invocation.arguments[0].call()

                standardOutput.write(" hello world\n".bytes)

                return null
            }
        })

        def commandRunner = new CommandRunner(project)
        def output = commandRunner.run("foo")

        assertEquals("foo", commandLineParams[0])
        assertEquals(" hello world\n", output)
    }

    private void commandLine(Object[] args) {
        commandLineParams = args
    }
}
