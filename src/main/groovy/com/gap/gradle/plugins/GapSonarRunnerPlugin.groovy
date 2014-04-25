package com.gap.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoMerge
import org.gradle.testing.jacoco.tasks.JacocoReport

class GapSonarRunnerPlugin implements Plugin<Project>{


    @Override
    void apply(Project project) {

        project.apply plugin: 'sonar-runner'
        project.allprojects{
            apply plugin: 'jacoco'
        }

        project.jacoco {
            toolVersion = "0.7.0.201403182114"
        }


        project.tasks.create(name:'jacocoMerge', type:JacocoMerge) {
                project.allprojects.each{
                    def testTasks = it.tasks.withType(Test)
                    project.jacocoMerge.dependsOn << testTasks
                    testTasks.each {
                        executionData it.jacoco.destinationFile
                    }
                }
        }

        project.tasks.create(name:'jacoco', type:JacocoReport, dependsOn:'jacocoMerge') {
            executionData project.jacocoMerge.destinationFile
            sourceDirectories = project.files()
            classDirectories = project.files()
            project.allprojects.each {
                if(it.plugins.hasPlugin('java')){
                    sourceDirectories += project.files(it.sourceSets.main.java.srcDirs)
                    classDirectories += project.files(it.sourceSets.main.output)
                }

            }
        }

        project.sonarRunner {
            sonarProperties{
                property "sonar.host.url", "http://sonar001.phx.gapinc.dev:9000/"
                property "sonar.jdbc.url", "jdbc:mysql://dgphxmetdb002.phx.gapinc.dev:3306/sonar"
                property "sonar.jdbc.driverClassName", "com.mysql.jdbc.Driver"
                property "sonar.jdbc.username", "sonar"
                property "sonar.jdbc.password", "sonar"
                property "sonar.projectName", project.name
                property "sonar.projectKey", "${project.group}:${project.name}"
                property "sonar.projectVersion", "$version"
                property "sonar.dynamicAnalysis", "reuseReports"
                property "sonar.java.coveragePlugin", "jacoco"
                property "sonar.java.jacoco.reportPath", project.jacocoReport.destinationFile
            }
        }

        project.tasks.sonarRunner.dependsOn << 'jacoco'
    }
}
