package helpers

import groovy.mock.interceptor.MockFor

import static junit.framework.Assert.assertFalse
import static org.hamcrest.MatcherAssert.assertThat
import static org.junit.internal.matchers.StringContains.containsString

class Assert {
    static def shouldExecuteTask(project, taskName, type) {
        def task = new MockFor(type)
        task.demand.execute {}
        task.use {
            project.tasks.findByName(taskName).execute()
        }
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
}
