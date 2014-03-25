package com.gap.gradle.plugins.cookbook

import com.gap.gradle.chef.CookbookUtil
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertThat

public class GenerateCookbookMetadataTaskTest {

    Project project
    Task metadataTask

    @Before
    void setUp() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapcookbook'
        metadataTask = project.tasks.findByName('generateCookbookMetadata')
    }

    @Test
    void shouldUseConfiguredCookbookDir() {
        def util = new MockFor(CookbookUtil)
        project.chef.cookbookDir = "/this/is/my/cookbook/dir"
        util.demand.metadataFrom(1) { dir ->
            assertThat(dir, equalTo(project.chef.cookbookDir))
        }
        util.use {
            metadataTask.execute()
        }
    }

    @Test
    void shouldSetMetadataOnProject() {
        def util = new MockFor(CookbookUtil)
        util.demand.metadataFrom() { dir -> [ "got metadata?": true ] }
        util.use {
            metadataTask.execute()
            assertThat(project.chef.metadata, equalTo([ "got metadata?": true ]))
        }
    }
}
