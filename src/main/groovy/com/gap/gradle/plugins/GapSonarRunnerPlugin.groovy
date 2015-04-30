package com.gap.gradle.plugins
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
                property "sonar.host.url", "http://sonar001.phx.gapinc.dev:9000/"
                property "sonar.jdbc.url", "jdbc:mysql://dgphxmetdb002.phx.gapinc.dev:3306/sonar"
                property "sonar.jdbc.driverClassName", "com.mysql.jdbc.Driver"
                property "sonar.jdbc.username", "sonar"
                property "sonar.jdbc.password", "sonar"
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
                    //Hardcoded to resolve issue with timezones.
                    //Loca Timezone & Pipeline server timezones are different and
                    //local analysis is running behind latest snapshot time.
                    property "sonar.projectDate", "${nextDay.format(SONAR_DATE_FORMAT)}"
                } else {
                    property "sonar.analysis.mode", "analysis"

                    SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyy-MM-dd");

                    String ecProjectName = commanderClient.getCurrentProjectName();
                    String ecSegmentName = commanderClient.getCurrentSegment();
                    String useSonarRunnerKey = "${ecProjectName}:${ecSegmentName}"
                    println("**** Project:Segment in SonarRunner is : " + useSonarRunnerKey + " ****")

                    String key = "/projects/WM Segment Registry/ApplySonarRunner/${useSonarRunnerKey}"
                    commanderClient.setECProperty(key,simpleDateFormat.format(new Date()));
                }
            }
        }

        project.tasks.sonarRunner.dependsOn << project.tasks.findAll { it.name.equals('test') }

        project.tasks.create(name: 'sonar', dependsOn: 'sonarRunner') << {
            new SonarLinkTask(project).execute()
        }
    }

    private boolean isLocal() {
        !new CommanderClient().isRunningInPipeline()
    }
}
