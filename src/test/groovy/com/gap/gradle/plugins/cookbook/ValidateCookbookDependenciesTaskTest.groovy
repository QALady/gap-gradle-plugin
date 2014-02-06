package com.gap.gradle.plugins.cookbook

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class ValidateCookbookDependenciesTaskTest {

    Project project
    Task validateTask

    @Before
    void setUp() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapcookbook'
        project.chef.requirePinnedDependencies = true
        validateTask = project.tasks.findByName('validateCookbookDependencies')
    }

    @Test
    void shouldSucceed_whenNoCookbookDependenciesDeclared() {
        project.chef.metadata = []
        validateTask.execute()
    }

    @Test
    void shouldSucceed_whenCookbookDependenciesEmpty() {
        project.chef.metadata = [ "dependencies": [:] ]
        validateTask.execute()
    }

    @Test
    void shouldSucceed_whenAllCookbookDependenciesHavePinnedVersions() {
        project.chef.metadata = [ "dependencies": [
            "gapTomcat": "0.0.28",
            "gapNagios": "0.0.1",
        ] ]
        validateTask.execute()
    }

    @Test(expected = Exception)
    void shouldFail_whenCookbookDependencyHasNoVersion() {
        project.chef.metadata = [ "dependencies": [
            "gapTomcat": "",
            "gapNagios": "0.0.1",
        ] ]
        validateTask.execute()
    }

    @Test(expected = Exception)
    void shouldFail_whenCookbookDependencyHasDynamicVersion() {
        project.chef.metadata = [ "dependencies": [
            "gapTomcat": "0.0.28",
            "gapNagios": ">= 0.0.1",
        ] ]
        validateTask.execute()
    }

    @Test(expected = Exception)
    void shouldFail_whenNoMetadataFoundOnProject() {
        validateTask.execute()
    }
}
