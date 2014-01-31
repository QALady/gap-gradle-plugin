package com.gap.gradle.plugins.cookbook
import com.gap.gradle.chef.CookbookUtil
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class ValidateCookbookDependenciesTaskTest {

    Project project
    Task validateTask
    def metadata
    def cookbookUtil

    @Before
    void setUp() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapcookbook'
        validateTask = project.tasks.findByName('validateCookbookDependencies')
        cookbookUtil = new MockFor(CookbookUtil)
        cookbookUtil.demand.metadataFrom() { dir -> metadata }
    }

    @Test
    void shouldSucceed_whenNoCookbookDependenciesDeclared() {
        metadata = []
        cookbookUtil.use {
            validateTask.execute()
        }
    }

    @Test
    void shouldSucceed_whenCookbookDependenciesEmpty() {
        metadata = [ "dependencies": [:] ]
        cookbookUtil.use {
            validateTask.execute()
        }
    }

    @Test
    void shouldSucceed_whenAllCookbookDependenciesHavePinnedVersions() {
        metadata = [ "dependencies": [
            "gapTomcat": "0.0.28",
            "gapNagios": "0.0.1",
        ] ]
        cookbookUtil.use {
            validateTask.execute()
        }
    }

    @Test(expected = Exception)
    void shouldFail_whenCookbookDependencyHasNoVersion() {
        metadata = [ "dependencies": [
            "gapTomcat": "",
            "gapNagios": "0.0.1",
        ] ]
        cookbookUtil.use {
            validateTask.execute()
        }
    }

    @Test(expected = Exception)
    void shouldFail_whenCookbookDependencyHasDynamicVersion() {
        metadata = [ "dependencies": [
            "gapTomcat": "0.0.28",
            "gapNagios": ">= 0.0.1",
        ] ]
        cookbookUtil.use {
            validateTask.execute()
        }
    }
}
