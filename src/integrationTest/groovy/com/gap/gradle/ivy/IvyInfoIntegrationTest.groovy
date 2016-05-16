package com.gap.gradle.ivy

import static org.hamcrest.CoreMatchers.is
import static org.junit.Assert.assertThat

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class IvyInfoIntegrationTest {

    @Test
    public void resolvedDependencies_shouldReturnAllTransitiveDependencies(){
        def project = ProjectBuilder.builder().build()
        def childProject = ProjectBuilder.builder().withName('child').withParent(project).build()

        project.configurations{
            archives
        }
        project.repositories{
            ivy {
                layout "maven"
                url "http://artifactory.gapinc.dev/artifactory/maven-repos"
            }
        }
        project.dependencies{
            archives group: 'com.gap.ref-app.iso', name:'ci', version: '1698', configuration: 'cookbooks'
        }

        childProject.configurations{
            archives
        }
        childProject.repositories{
            ivy {
                layout "maven"
                url "http://artifactory.gapinc.dev/artifactory/maven-repos"
            }
        }
        childProject.dependencies{
            archives group: 'com.gap.test_wmsegment', name:'test_wmsegment', version: '254', configuration: 'default'
        }

        project.configurations.archives.resolve()
        def ivyInfo = new IvyInfo(project)
        def resolvedDependencies = ivyInfo.allResolvedDependencies
        def expectedDependencies = ["com.gap.ref-app.iso:ci:1698", "com.gap.ref-app.infra:ci:0.0.46.2407750", "com.gap.test_wmsegment:test_wmsegment:254"]
        assertThat(resolvedDependencies, is(expectedDependencies))
    }
}
