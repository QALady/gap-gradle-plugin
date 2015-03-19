package com.gap.gradle.tasks

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.hamcrest.CoreMatchers.containsString
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail
import static org.testng.Assert.assertEquals
import static org.testng.Assert.assertNotEquals

public class StartBackgroundProcessTaskTest {

    private static final String DUMMY_PROCESS_FILE = "src/test/groovy/com/gap/gradle/resources/dummy_process.sh"

    private Project project
    private Task task
    private File dummyProcessFile

    @Before
    public void setUp() throws Exception {
        dummyProcessFile = new File(DUMMY_PROCESS_FILE)
        dummyProcessFile.setExecutable(true)

        project = ProjectBuilder.builder().build()
        task = project.task('startProcess', type: StartBackgroundProcessTask)
    }

    @Test(timeout = 2000l)
    public void shouldStartProcessAndWritePidToFile() throws Exception {
        def pidFile = tempFile()

        assertEquals(0, pidFile.length())

        task.command "sh ${DUMMY_PROCESS_FILE}"
        task.pidFile pidFile
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
