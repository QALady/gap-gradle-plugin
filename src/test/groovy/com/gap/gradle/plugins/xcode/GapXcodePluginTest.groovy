package com.gap.gradle.plugins.xcode
import com.gap.gradle.plugins.mobile.CommandRunner
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.internal.reflect.Instantiator
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static helpers.Assert.*
import static org.hamcrest.CoreMatchers.containsString
import static org.junit.Assert.*
import org.apache.commons.io.FileUtils
import groovy.mock.interceptor.MockFor


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
        taskShouldExist('replaceTokensInSettingsBundle', project)
        taskShouldExist('buildHybridSDK', project)
    }

    @Test
    public void shouldAddNewConfigurations() throws Exception {
        projectShouldHaveConfiguration(project, 'airwatchConfig')
    }

    @Test
    public void shouldConfigureTaskDependencies() throws Exception {
        taskShouldDependOn('uploadArchives', 'airwatchConfigZip', project)
        taskShouldDependOn('xcodebuild', 'keychainCreate', project)
        taskShouldDependOn('xcodebuild', 'provisioningInstall', project)
        taskShouldBeFinalizedBy('xcodebuild', 'replaceTokensInSettingsBundle', project)
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
    public void shouldRetriveSigningIdentityByName() throws Exception {
        project.xcode {
            signing {
                myIdentity {
                    description "a test identity"
                }
            }

            build {
                signingIdentity "myIdentity"
            }
        }

        def identity = project.xcode.build.signingIdentity

        assertEquals("myIdentity", identity.name)
        assertEquals("a test identity", identity.description)
    }

    @Test
    public void shouldAliasCleaningTasksToKeepBackwardsCompatibility() throws Exception {
        taskShouldDependOn('keychain-clean', 'keychainClean', project)
        taskShouldDependOn('provisioning-clean', 'provisioningClean', project)
    }

    @Test
    public void shouldReplaceTokensInSettingsBundle() throws Exception {
        project.xcode {
            build {
                productName 'MyAppProductName'
                target 'MyApp'
                sdk 'iphoneos'
                signingIdentity signing.development
            }

            archive {
                version '1'
                scmRevision 'foobar'
            }
        }

        def plist = new File("${project.buildDir}/MyAppProductName/Release-iphoneos/MyAppProductName.app/Settings.bundle", 'Root.plist')
        plist.parentFile.mkdirs()
        plist << '@version@'
        plist << '@scmRevision@'

        project.tasks['replaceTokensInSettingsBundle'].execute()

        assertThat(plist.text, containsString('1'))
        assertThat(plist.text, containsString('foobar'))
    }

}
