package helpers

import groovy.mock.interceptor.MockFor

class Assert {
    static def shouldExecuteTask(project, taskName, type) {
        def task = new MockFor(type)
        task.demand.execute {}
        task.use {
            project.tasks.findByName(taskName).execute()
        }
    }
}
