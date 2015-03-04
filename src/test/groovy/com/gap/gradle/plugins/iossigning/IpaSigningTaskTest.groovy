package com.gap.gradle.plugins.iossigning

import com.gap.gradle.plugins.iossigning.exceptions.ArtifactNotFoundException
import com.gap.gradle.plugins.xcode.SigningIdentity
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test

import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail

class IpaSigningTaskTest {
    private project

    @Before
    public void setUp() throws Exception {
        project = new ProjectBuilder().build()
        project.apply(plugin: 'gap-ios-signing')
    }

    @Test
    public void shouldSkipTaskIfRequiredParamsWereNotSpecified() throws Exception {
        def task = project.ipaSigning
        task.execute()

        assertThat(task.state.skipped, Matchers.is(true))
    }

    @Test
    public void shouldHavePreConfiguredSigningIdentities() throws Exception {
        assertThat(project.ipaSigning.signing.development, instanceOf(SigningIdentity))
        assertThat(project.ipaSigning.signing.distribution, instanceOf(SigningIdentity))
    }

    @Test
    public void shouldThrowExceptionIfNoArtifactWasFoundToBeSigned() throws Exception {
        project.configurations.create('archives')
        project.ipaSigning {
            signingIdentity signing.development
            artifact.classifier 'foo'
        }

        try {
            project.ipaSigning.execute()
            fail("Should throw exception because there are no artifacts to be signed.")
        } catch (e) {
            assertThat(e.cause, instanceOf(ArtifactNotFoundException))
        }
    }
}
