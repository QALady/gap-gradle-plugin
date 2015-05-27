package com.gap.gradle.plugins.cookbook

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.hamcrest.Matchers.containsString
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail

class ValidateCookbookDependenciesTaskIntegrationTest {

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder()

    Project project
    Task metadataTask
    Task validateTask

    @Before
    void setUp() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapcookbook'
        project.chef.cookbookDir = tempFolder.root.absolutePath
        project.chef.requirePinnedDependencies = true
        metadataTask = project.tasks.findByName("generateCookbookMetadata")
        validateTask = project.tasks.findByName("validateCookbookDependencies")
    }

    @Test
    void shouldSucceed_whenCookbookDependenciesHavePinnedVersions() {
        createMetadataRb([
            "version '999.99.9999'",
            "name    'ref-app'",
            //"depends 'gapTomcat', '0.0.23'",
            "depends 'gapNagios', '>= 0.0.1'",
        ])
        executeTasks()
    }

    @Test
    void shouldSucceed_whenCookbookHasNoDeclaredDependencies() {
        createMetadataRb([
            "version '999.99.9999'",
            "name    'ref-app'",
        ])
        executeTasks()
    }

    @Test
    void shouldFail_whenCookbookDependencyVersionContainsRange() {
        try {
            createMetadataRb([
                "version '999.99.9999'",
                "name    'ref-app'",
                //"depends 'gapTomcat', '0.0.23'",
                "depends 'gapNagios', '>= 0.0.1'",
            ])
            executeTasks()
            fail("No exception thrown!")
        } catch (Throwable throwable) {
            assertCause(throwable, UnpinnedDependencyException,"Cookbook dependency 'gapNagios' version '>= 0.0.1' is not pinned")
        }
    }

    @Test
    void shouldFail_whenCookbookDependencyVersionIsMissing() {
        try {
            createMetadataRb([
                "version '999.99.9999'",
                "name    'ref-app'",
                //"depends 'gapTomcat', '0.0.23'",
                "depends 'gapNagios'",
            ])
            executeTasks()
            fail("No exception thrown!")
        } catch (Throwable throwable) {
            assertCause(throwable, UnpinnedDependencyException, "Cookbook dependency 'gapNagios' version '>= 0.0.0' is not pinned")
        }
    }

    def executeTasks() {
        metadataTask.execute()
        validateTask.execute()
    }

    def createMetadataRb(List<String> lines) {
        tempFolder.newFile("metadata.rb").write(lines.join('\n'))
    }

    static def assertCause(Throwable throwable, Class<?> type, String message) {
        assertThat(extractCause(throwable, type).message, containsString(message))
    }

    static def extractCause(Throwable throwable, Class<?> type) {
        Throwable originalException = throwable
        Throwable extractedException = throwable
        while (!(type.isInstance(extractedException))) {
            if (extractedException.cause == null) {
                throw originalException
            }
            extractedException = extractedException.cause
        }
        extractedException
    }
}
