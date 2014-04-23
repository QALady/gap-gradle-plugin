package com.gap.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import net.saliman.gradle.plugin.cobertura.CoberturaPlugin

class GapSonarRunnerPlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {

        project.apply plugin: 'sonar-runner'
        project.apply plugin: CoberturaPlugin

        project.cobertura {
            coverageFormats = ['xml', 'html']
            coverageReportDir = new File("${project.rootDir}/target/reports/coverage")
            project.subprojects.each {
                coverageDirs <<  it.sourceSets.main.classesDir.path
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
                property "sonar.java.coveragePlugin", "cobertura"
                property "sonar.java.cobertura.reportPath", "target/reports/coverage/coverage.xml"
            }
        }
    }
}
