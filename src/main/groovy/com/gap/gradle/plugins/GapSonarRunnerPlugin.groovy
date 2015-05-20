package com.gap.gradle.plugins

import com.gap.gradle.exceptions.SonarRunnerUndefinedProjectVersionException
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
                property "sonar.profile", "AAD Reviewed Rules"
                property "sonar.dynamicAnalysis", "reuseReports"
                property "sonar.java.coveragePlugin", "jacoco"
                property "sonar.java.jacoco.reportPath", project.jacoco.reportsDir
                property "sonar.issuesReport.html.enable", "true"
                if (isLocal()) {
                    property "sonar.analysis.mode", "preview"
                    //Disabling a Source Control and Issue plugin for preview mode to work
                    property 'sonar.scm.enabled', 'false'
                    property 'sonar.scm-stats.enabled', 'false'
                    property 'issueassignplugin.enabeld', 'false'

                    //Hardcoded to resolve issue with timezones.
                    //Loca Timezone & Pipeline server timezones are different and
                    //local analysis is running behind latest snapshot time.
                    property "sonar.projectDate", "${nextDay.format(SONAR_DATE_FORMAT)}"
                } else {
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

        project.tasks.create(name: 'saveSonarProperty') << {
            if (!isLocal()) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String ecSegmentName = commanderClient.getCurrentSegment();
                println("**** Project:Segment in SonarRunner is : " + ecSegmentName + " ****")

                String key = "/projects/WM Segment Registry/ApplySonarRunner/${ecSegmentName}".toString()
                commanderClient.setECProperty(key, simpleDateFormat.format(new Date()));
            }
        }

        project.tasks.create(name: 'checkProjectVersion') << {
            if (!isLocal()) {
                def projectVersion = getSonarProperty(project, "sonar.projectVersion");
                println "projectCurrentVersion is '$projectVersion'"

                if (projectVersion == null || projectVersion.toString().trim().isEmpty() || projectVersion.toString().toLowerCase().equalsIgnoreCase("unspecified")) {
                    def version = commanderClient.getECProperty("/myJob/version")
                    if (version == null || version.trim().isEmpty()) {
                        throw new SonarRunnerUndefinedProjectVersionException("'version' parameter is not passed to " +
                                "sonar task and /myJob/version also doesn't exits. Please run it in a segment or pass " +
                                "the version.".toString())
                    }
                    project.sonarRunner {
                        sonarProperties {
                            property "sonar.projectVersion", version
                        }
                    }
                } else if (projectVersion.toString().toLowerCase().contains("dev")
                        || projectVersion.toString().toLowerCase().contains("local")) {
                    throw new SonarRunnerUndefinedProjectVersionException("Invalid version to publish sonar report: $projectVersion".toString())
                }
            }
        }

        project.tasks.sonarRunner.dependsOn << project.tasks.saveSonarProperty
        //project.tasks.sonarRunner.dependsOn << project.tasks.checkProjectVersion
    }

    private boolean isLocal() {
        !new CommanderClient().isRunningInPipeline()
    }

    private def getSonarProperty(def project, String key) {
        project.tasks.sonarRunner.sonarProperties.getProperty(key)
    }
}
