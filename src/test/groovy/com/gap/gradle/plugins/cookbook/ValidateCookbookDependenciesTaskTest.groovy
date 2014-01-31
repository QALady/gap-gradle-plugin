package com.gap.gradle.plugins.cookbook
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static org.junit.Assert.fail

class ValidateCookbookDependenciesTaskTest {

    Project project
    Task validateTask

    @Before
    void setUp() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapcookbook'
        validateTask = project.tasks.findByName('validateCookbookDependencies')
    }

    @Ignore
    @Test
    void shouldNotThrowException_whenNoCookbookDependencies() {
        fail()
    }

    @Ignore
    @Test
    void shouldNotThrowException_whenAllCookbookDependenciesHavePinnedVersions() {
        fail()
    }

    @Ignore
    @Test
    void shouldThrowException_whenCookbookDependencyHasNoVersion() {
        fail()
    }

    @Ignore
    @Test
    void shouldThrowException_whenCookbookDependencyHasDynamicVersion() {
        fail()
    }
}
