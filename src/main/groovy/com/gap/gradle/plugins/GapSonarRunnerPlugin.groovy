package com.gap.gradle.plugins
import com.gap.pipeline.tasks.SonarLinkTask
import org.gradle.api.Plugin
import org.gradle.api.Project

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


        project.subprojects{ subProj ->
            if(plugins.hasPlugin('java')){
                sonarRunner{
                    sonarProperties{
                        property "sonar.junit.reportsPath", test.reports.junitXml.destination
                    }
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
                property "sonar.junit.reportsPath", "${project.buildDir}/test-results"
                property "sonar.projectName", project.name
                property "sonar.projectKey", "${project.group}:${project.name}"
                property "sonar.dynamicAnalysis", "reuseReports"
                property "sonar.java.coveragePlugin", "jacoco"
                property "sonar.java.jacoco.reportPath", project.jacoco.reportsDir
            }
        }


        project.tasks.create(name: 'sonar', dependsOn:'sonarRunner') << {
            new SonarLinkTask(project).execute()
        }
    }
}
