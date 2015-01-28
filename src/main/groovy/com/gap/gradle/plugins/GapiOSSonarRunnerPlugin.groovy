package com.gap.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy

class GapiOSSonarRunnerPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.apply plugin: 'gap-sonar-runner'

        project.configurations.create('sonarBuildWrapperSource')

        project.dependencies.add('sonarBuildWrapperSource', [group: 'sonarqube', name: 'build-wrapper', ext: 'zip', version: '3.2'])

        project.task('extractSonarBuildWrapper', type: Copy) {
            from {
                project.configurations.sonarBuildWrapperSource.collect { project.zipTree(it) }
            }
            into 'sonar'
        }

        project.sonarRunner {
            sonarProperties {
                property "sonar.cfamily.build-wrapper-output", "build/build_wrapper_output"
                property "sonar.language", "objc"
                property "sonar.profile", "Sonar way"
                property "sonar.dynamicAnalysis", "reuseReports"
            }
        }

        project.tasks.sonarRunner.dependsOn.clear()

        project.afterEvaluate {
            configureGcovReportsPath(it)
        }
    }

    private void configureGcovReportsPath(project) {
        def gcovTask = project.tasks.findByName('gcovCoverage')

        if (gcovTask) {
            project.sonarRunner {
                sonarProperties {
                    property "sonar.cfamily.gcov.reportsPath", gcovTask.reportsDir
                }
            }
        }
    }

}
