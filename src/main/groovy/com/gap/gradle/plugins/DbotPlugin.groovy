package com.gap.gradle.plugins
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator
import org.gradle.api.tasks.JavaExec

import javax.inject.Inject

class DbotPlugin implements Plugin<Project> {

    private final Instantiator instantiator
    private Project project

    @Inject
    public DbotPlugin(Instantiator instantiator) {
        this.instantiator = instantiator
    }

    @Override
    void apply(Project project) {
        project.plugins.apply('liquibase')
        this.project = project

        project.afterEvaluate {
          createTasks()
        }
    }

    void createTasks() {
        project.task("generateChangeLogDBOT", type: JavaExec) {
          classpath project.configurations.runtime
            main = 'com.gap.gid.dbot.Main'
            doFirst {
              args = [
                "--username=" + project.ext.username,
                "--password=" + project.ext.password,
                "--url=" + project.ext.url,
                "--schema=" + project.ext.schema,
                "--driver=" + project.ext.driver,
                "--flavor=" + project.ext.flavor,
                ].toList()
            }
        }
    }
}
