package com.gap.gradle.plugins.cookbook
import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail

import com.gap.gradle.utils.ShellCommand
import groovy.json.JsonBuilder
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ValidateTransitiveCookbookDependenciesTaskTest {

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder()

    Project project
    Task validateTask
    def command

    @Before
    void setUp() {
        project = ProjectBuilder.builder().build();
        project.apply plugin: 'gapcookbook'
        project.chef.requireTransitiveDependencies = true
        project.chef.cookbookDir = tempFolder.root.absolutePath
        validateTask = project.tasks.findByName('validateTransitiveCookbookDependencies')
        command = new MockFor(ShellCommand)
        command.ignore.execute { input, env = null -> }
    }

    @Test
    void shouldSucceed_whenMetadataDeclaresNoDependencies() {
        project.chef.metadata = [ "dependencies": [:] ]
        command.use {
            validateTask.execute()
        }
    }

    @Test
    void shouldCreateBerksfileWithDependencies() {
        project.chef.metadata = [ "dependencies": [
            "gapJava": "0.0.11",
            "gapTomcat": "0.0.28",
        ]]
        command.use {
            createLockFile()
            validateTask.execute()
        }
        assertThat(new File(tempFolder.root, "berks/Berksfile").text, equalTo([
            "chef_api :config\n",
            "cookbook 'gapJava', '0.0.11'\n",
            "cookbook 'gapTomcat', '0.0.28'\n"
        ].join('')))
    }

    @Test
    void shouldFail_whenResolvedVersionDoesNotExistInProduction() {
        project.chef.metadata = [ "dependencies": [
            "gapJava": "0.0.11",
            "gapTomcat": "0.0.28",
        ]]
        command.ignore.execute { input, env = null ->
            if (input == "knife cookbook show gapJava") {
                return "gapJava  0.0.11  0.0.10"
            } else if (input == "knife cookbook show gapTomcat") {
                return "gapTomcat 0.0.27 0.0.26 0.0.25"
            }
        }
        command.use {
            try {
                createLockFile([
                    "gapJava@0.0.13",
                    "gapTomcat@0.0.28",
                ]);
                validateTask.execute()
                fail("No exception thrown!")
            } catch (Exception exception) {
                assertCause(exception, UnpinnedDependencyException,
                    "These transitive dependencies don't exist in prod:"
                    + " gapTomcat@0.0.28 (latest is 0.0.27)"
                    + ", gapJava@0.0.13 (latest is 0.0.11)")
            }
        }
    }

    def createLockFile(dependencies = []) {
        def sources = [:]
        dependencies.each { dependency ->
            def result = dependency.split("@")
            sources[result[0]] = [
                "locked_version": result[1],
            ]
        }
        new File(tempFolder.root, "berks").mkdir()
        new File(tempFolder.root, "berks/Berksfile.lock").write(new JsonBuilder([ "sources": sources ]).toString())
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
