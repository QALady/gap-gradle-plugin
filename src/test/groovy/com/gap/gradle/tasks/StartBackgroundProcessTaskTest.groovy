package com.gap.gradle.tasks

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.hamcrest.CoreMatchers.containsString
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail

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
        task = project.task('startProcess', type: SpawnBackgroundProcessTask)
    }

    @Test(timeout = 2000l)
    public void shouldStartProcess() throws Exception {
        task.command "sh ${DUMMY_PROCESS_FILE}"

        task.execute()
    }

    @Test
    public void shouldThrowExceptionIfTaskNotConfigured() throws Exception {
        try {
            task.execute()

            fail('Exception not thrown')
        } catch (Exception e) {
            assertThat(e.cause.message, containsString('command'))
        }
    }

    @Test 
    public void shouldThrowMaxRetriesExceptionWhenProcessNotFound() throws Exception {
        try {
               task.command "echo test"
               task.execute()
            }
        catch(Exception e) {
           assertThat(e.cause.message,containsString('Process Timedout: echo test didnt start in 60 secs'))
        }
    }
}
