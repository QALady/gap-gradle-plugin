package com.gap.gradle.plugins.xcode

import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.internal.reflect.Instantiator
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static helpers.Assert.*
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class GapXcodePluginTest {

    private Project project
    private GapXcodePlugin plugin

    @Before
    public void setUp() throws Exception {
        project = ProjectBuilder.builder().build()

        Instantiator instantiator = ((ProjectInternal) project).getServices().get(Instantiator)
        plugin = new GapXcodePlugin(instantiator)

        plugin.apply(project)
    }

    @Test
    public void shouldAddNewTasks() throws Exception {
        taskShouldExist('airwatchConfigZip', project)
        taskShouldExist('transformJUnitXmlReportToHTML', project)
        taskShouldExist('gcovAnalyze', project)
    }

    @Test
    public void shouldAddNewConfigurations() throws Exception {
        projectShouldHaveConfiguration(project, 'airwatchConfig')
    }

    @Test
    public void shouldConfigureTaskDependencies() throws Exception {
        taskShouldDependOn('uploadArchives', 'airwatchConfigZip', project)
        taskShouldDependOn('gcovAnalyze', 'test', project)
        taskShouldBeFinalizedBy('test', 'transformJUnitXmlReportToHTML', project)
    }

    @Test
    public void shouldCreateDefaultSigningIdentities() throws Exception {
        assertNotNull('Should have added developer identity', project.xcode.signing.development)
        assertNotNull('Should have added distribution identity', project.xcode.signing.distribution)
    }

    @Test
    public void shouldSupportAddingNewSigningIdentities() throws Exception {
        project.xcode {
            signing {
                testing {
                    description 'a test identity'
                    certificateURI 'foo'
                    certificatePassword 'secret'
                    mobileProvisionURI 'bar'
                }
            }
        }

        def testIdentity = project.xcode.signing.testing
        assertEquals('testing', testIdentity.name)
        assertEquals('a test identity', testIdentity.description)
        assertEquals('foo', testIdentity.certificateURI)
        assertEquals('secret', testIdentity.certificatePassword)
        assertEquals('bar', testIdentity.mobileProvisionURI)
    }

    @Test
    public void shouldAllowEditSigningIdentities() throws Exception {
        project.xcode {
            signing {
                testing {
                    description 'a test identity'
                }
            }
        }

        project.xcode {
            signing {
                testing {
                    description 'foo'
                    certificateURI 'bar'
                }
            }
        }

        def testIdentity = project.xcode.signing.testing
        assertEquals('foo', testIdentity.description)
        assertEquals('bar', testIdentity.certificateURI)
    }

    @Test
    public void shouldAliasCleaningTasksToKeepBackwardsCompatibility() throws Exception {
        taskShouldDependOn('keychain-clean', 'keychainClean', project)
        taskShouldDependOn('provisioning-clean', 'provisioningClean', project)
    }
}
