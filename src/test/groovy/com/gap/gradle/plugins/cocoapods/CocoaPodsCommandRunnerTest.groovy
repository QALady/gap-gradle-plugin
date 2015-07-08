package com.gap.gradle.plugins.cocoapods

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

@Ignore //due to 2.4 Gradle update
public class CocoaPodsCommandRunnerTest {

    private String[] commandLineParams
    private CocoaPodsCommandRunner cocoapods

    @Before
    public void setUp() throws Exception {
        Project project = mock(Project)

        when(project.exec(any(Closure))).thenAnswer(new Answer<ExecResult>() {
            @Override
            ExecResult answer(InvocationOnMock invocation) throws Throwable {
                invocation.arguments[0].delegate = this
                invocation.arguments[0].call()

                return null
            }
        })

        cocoapods = new CocoaPodsCommandRunner(project)
    }

    @Test
    public void shouldListAvailableRepos() throws Exception {
        cocoapods.listRepos()

        assertEquals("pod", commandLineParams[0])
        assertEquals("repo", commandLineParams[1])
        assertEquals("list", commandLineParams[2])
    }

    @Test
    public void shouldPushPodspecToRepo() throws Exception {
        def fakePodspec = mock(File)
        when(fakePodspec.absolutePath).thenReturn("/path/to/some.podspec")

        cocoapods.pushPodspec(fakePodspec, "someRepo")

        assertEquals("pod", commandLineParams[0])
        assertEquals("repo", commandLineParams[1])
        assertEquals("push", commandLineParams[2])
        assertEquals("someRepo", commandLineParams[3])
        assertEquals(fakePodspec.absolutePath, commandLineParams[4])
    }

    private void commandLine(Object[] args) {
        commandLineParams = args
    }
}
