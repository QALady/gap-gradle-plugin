package com.gap.gradle.plugins.cookbook

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@Ignore
class ValidateTransitiveCookbookDependenciesTaskIntegrationTest {

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
        project.chef.requireTransitiveDependencies = true
        metadataTask = project.tasks.findByName("generateCookbookMetadata")
        validateTask = project.tasks.findByName("validateTransitiveCookbookDependencies")
    }

    @Test
    void shouldVerifyCookbooksExist() {
        createMetadataRb([
            "version '999.99.9999'",
            "name    'ref-app'",
            "depends 'gapTomcat', '0.0.23'",
            "depends 'gapNagios', '0.4.338'",
        ])
        executeTasks()
    }

    def createMetadataRb(List<String> lines) {
        tempFolder.newFile("metadata.rb").write(lines.join('\n'))
    }

    def executeTasks() {
        metadataTask.execute()
        validateTask.execute()
    }
}
