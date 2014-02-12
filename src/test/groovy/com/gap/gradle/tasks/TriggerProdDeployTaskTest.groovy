package com.gap.gradle.tasks

import org.gradle.api.Project
import org.gradle.api.Task
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

@Ignore
class TriggerProdDeployTaskTest{

    private Task triggerProdDeployTask
    private Project project

    @Before
    void setup(){
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapcookbook'
        triggerProdDeployTask = project.tasks.findByName('triggerProdDeployTask')
    }

    @Test
    void shouldLoadConfig() {
        triggerProdDeployTask.execute()
    }
}