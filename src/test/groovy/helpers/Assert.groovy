package helpers
import static junit.framework.Assert.assertFalse
import static org.hamcrest.CoreMatchers.nullValue
import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.notNullValue
import static org.junit.Assert.assertThat

import groovy.mock.interceptor.MockFor

class Assert {
    static def shouldExecuteTask(project, taskName, type) {
        def task = new MockFor(type)
        task.demand.execute {}
        task.use {
            project.tasks.findByName(taskName).execute()
        }
    }

    static def shouldNotExecuteTask(project, taskName, type) {
        def task = new MockFor(type)
        task.use {
            project.tasks.findByName(taskName).execute()
        }
    }

    static def taskShouldExist(task, project) {
        assertThat("Task '${task}' does not exist on project", project.tasks.findByName(task), notNullValue())
    }

    static def taskShouldNotExist(task, project) {
        assertThat("Task '${task}' exists on project", project.tasks.findByName(task), nullValue())
    }

    static void assertThrowsExceptionWithMessage(expectedMessage, Closure closure){
        try{
            closure()
            assertFalse("Expected exception with message '${expectedMessage} but got none", true)
        }
        catch(Exception ex){
            assertThat(ex.dump(), containsString(expectedMessage))
        }
    }

    static void taskShouldDependOn(task, requiredDependency, project) {
        for (def dependency : project.tasks.findByName(task).dependsOn) {
            if (dependency == requiredDependency) {
                return
            } else if (dependency instanceof List) {
                for (def d : dependency) {
                    if (d == requiredDependency) {
                        return
                    }
                }
            }
        }
        fail("Task ${task} does not declare a dependency on ${requiredDependency}")
    }
}
