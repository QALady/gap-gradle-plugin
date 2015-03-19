package helpers

import org.gradle.api.Project

import static org.hamcrest.CoreMatchers.nullValue
import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.notNullValue
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail

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

    static void taskShouldBeFinalizedBy(task, requiredFinalizer, project) {
        for (def finalizer : project.tasks.findByName(task).finalizedBy.values) {
            if (finalizer == requiredFinalizer) {
                return
            } else if (finalizer instanceof List) {
                for (def f : finalizer) {
                    if (f == requiredFinalizer) {
                        return
                    }
                }
            }
        }
        fail("Task ${task} is not finalized by ${requiredFinalizer}")
    }

    static void projectShouldHaveConfiguration(project, requiredConfiguration) {
        for (def configuration : project.configurations) {
            if (configuration.name == requiredConfiguration) {
                return
            }
        }
        fail("Project ${project} doesn't have configuration ${requiredConfiguration}")
    }

    static void taskShouldBeOfType(String taskName, Class<?> taskType, Project project) {
        def task = project.tasks.findByName(taskName)

        assertThat(task, instanceOf(taskType))
    }
}
