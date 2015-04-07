package com.gap.gradle.tasks

import com.gap.gradle.utils.Barrier
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import java.util.concurrent.TimeUnit

import static org.hamcrest.CoreMatchers.containsString
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail
import static org.testng.Assert.assertEquals
import static org.testng.Assert.assertNotEquals

public class StartBackgroundProcessTaskTest {

    private Task task
    private Barrier fakeBarrier

    @Before
    public void setUp() throws Exception {
        def project = ProjectBuilder.builder().build()
        task = project.task('startProcess', type: StartBackgroundProcessTask)
        fakeBarrier = new Barrier(1, 1, TimeUnit.SECONDS)
    }

    @Ignore("This test fails randomly on CI with a Process Timeout. Needs investigation.")
    @Test
    public void shouldStartProcessAndWritePidToFile() throws Exception {
        def pidFile = tempFile()

        assertEquals(0, pidFile.length())

        task.command 'sleep 30'
        task.pidFile pidFile
        task.barrier = fakeBarrier
        task.execute()

        assertNotEquals(0, pidFile.length())
    }

    @Test
    public void shouldThrowExceptionIfTaskNotConfigured() throws Exception {
        try {
            task.execute()

            fail('Exception not thrown')
        } catch (Exception e) {
            assertThat(e.cause.message, containsString('command'))
            assertThat(e.cause.message, containsString('pidFile'))
        }
    }

    @Test
    public void shouldThrowMaxRetriesExceptionWhenProcessNotFound() throws Exception {
        try {
            task.command "echo test"
            task.pidFile tempFile()
            task.barrier = fakeBarrier
            task.execute()

            fail('Exception not thrown')
        } catch (GradleException e) {
            assertThat(e.cause.message, containsString("echo test"))
        }
    }

    private static File tempFile() {
        return File.createTempFile('test', 'file')
    }
}
