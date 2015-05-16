package com.gap.gradle.plugins

import com.gap.gradle.tasks.GapSonarRunnerAuditorTask
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.tasks.SonarLinkTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.text.SimpleDateFormat

class GapSonarRunnerPlugin implements Plugin<Project> {

    private static final String SONAR_DATE_FORMAT = "YYYY-MM-dd"
    CommanderClient commanderClient = new CommanderClient();

    @Override
    void apply(Project project) {

        project.apply plugin: 'sonar-runner'
        project.allprojects {
            apply plugin: 'jacoco'
        }

        project.jacoco {
            toolVersion = "0.7.0.201403182114"
        }

        project.subprojects { subProj ->
            if (plugins.hasPlugin('java')) {
                subProj.sonarRunner {
                    sonarProperties {
                        property "sonar.junit.reportsPath", test.reports.junitXml.destination
                    }
                }
            }
        }

        def nextDay = new Date().next()

        project.sonarRunner {
            sonarProperties {
                property "sonar.host.url", "http://sonar.gapinc.dev/"
                property "sonar.jdbc.url", "jdbc:mysql://sonardb001.phx.gapinc.dev:3306/sonar"
                property "sonar.jdbc.driverClassName", "com.mysql.jdbc.Driver"
                property "sonar.jdbc.username", "sonar"
                property "sonar.jdbc.password", "SonarWM1969"
                property "sonar.junit.reportsPath", "${project.buildDir}/test-results"
                property "sonar.projectName", project.name
                property "sonar.projectKey", "${project.group}:${project.name}"
                property "sonar.profile", "GID Reviewed Rules"
                property "sonar.dynamicAnalysis", "reuseReports"
                property "sonar.java.coveragePlugin", "jacoco"
                property "sonar.java.jacoco.reportPath", project.jacoco.reportsDir
                property "sonar.issuesReport.html.enable", "true"
                if (isLocal()) {
                    property "sonar.analysis.mode", "preview"

                    //Hardcoded to resolve issue with timezones.
                    //Loca Timezone & Pipeline server timezones are different and
                    //local analysis is running behind latest snapshot time.
                    property "sonar.projectDate", "${nextDay.format(SONAR_DATE_FORMAT)}"
                } else {
                    def version = commanderClient.getECProperty("/myJob/version")
                    property "sonar.projectVersion", version
                    property "sonar.analysis.mode", "analysis"
                }
            }
        }

        project.tasks.sonarRunner.dependsOn << project.tasks.findAll { it.name.equals('test') }

        project.tasks.create(name: 'sonar', dependsOn: 'sonarRunner') << {
            new SonarLinkTask(project).execute()
        }

        project.task('gapSonarRunnerAuditor') << {
            new GapSonarRunnerAuditorTask(project).execute()
        }

        project.tasks.create(name: 'checkProjectVersion') << {
            def projectVersion = project.sonarRunner.sonarProperties.sonar.projectVersion
            println "projectCurrentVersion is " + projectVersion
            if (projectVersion==null|| projectVersion.toString().trim().isEmpty() || "dev".equalsIgnoreCase(projectVersion) || "unspecified".equalsIgnoreCase(projectVersion) || "local".equalsIgnoreCase(projectVersion) ) {
                //throw new SonarRunnerUndefinedProjectVersionException("Project Version is :${projectVersion}")
                println "Should trigger an SonarRunnerUndefinedProjectVersionException $projectVersion"
            }else{
                println "Project version is ok ${projectVersion}"
            }
        }

        project.tasks.create(name: 'saveSonarProperty') << {
            if (!isLocal()) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String ecSegmentName = commanderClient.getCurrentSegment();
                println("**** Project:Segment in SonarRunner is : " + ecSegmentName + " ****")

                String key = "/projects/WM Segment Registry/ApplySonarRunner/${ecSegmentName}".toString()
                commanderClient.setECProperty(key, simpleDateFormat.format(new Date()));
            }
        }

        project.tasks.sonarRunner.dependsOn << project.tasks.saveSonarProperty
        project.tasks.sonarRunner.dependsOn << project.tasks.checkProjectVersion
    }

    private static boolean isLocal() {
        !new CommanderClient().isRunningInPipeline()
    }
}
