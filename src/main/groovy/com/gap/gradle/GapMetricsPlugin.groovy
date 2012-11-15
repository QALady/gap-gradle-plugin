package com.gap.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.tasks.testing.Test

class GapMetricsPlugin implements Plugin<Project>{
  void apply(Project project) {

    def sonarUrl          = project.hasProperty('sonarUrl')         ? project.getProperty('sonarUrl')         : "http://ci.gap.dev/sonar";
    def sonarDatabaseUrl  = project.hasProperty('sonarDatabaseUrl') ? project.getProperty('sonarDatabaseUrl') : "jdbc:mysql://ci.gap.dev:3306/sonar?";
    def sonarUsername     = project.hasProperty('sonarUsername')    ? project.getProperty('sonarUsername')    : ""
    def sonarPassword     = project.hasProperty('sonarPassword')    ? project.getProperty('sonarPassword')    : ""
    def sonarDryRun       = project.hasProperty('sonarDryRun')      ? project.getProperty('sonarDryRun')      : true
    def coberturaExcludes = project.hasProperty('coberturaExcludes')? project.getProperty('coberturaExcludes'): ""

    if (project.plugins.hasPlugin('java')) {

      project.configure(project) {

        configurations {
          coberturaRuntime { extendsFrom testRuntime }
        }

        dependencies {
          coberturaRuntime 'net.sourceforge.cobertura:cobertura:1.9.3'
        }

        project.task('coberturaPrepare') {
          ext.datafileLocation = "${project.buildDir}/cobertura.ser"
          doLast {
            ant.taskdef(name: 'coberturaInstrument', classname: 'net.sourceforge.cobertura.ant.InstrumentTask',
              classpath: configurations.coberturaRuntime.asPath)
            ant.taskdef(name: 'coberturaReport', classname: 'net.sourceforge.cobertura.ant.ReportTask',
              classpath: configurations.coberturaRuntime.asPath)
          }
        }

        project.task('coberturaInstrument', dependsOn: ['classes', 'coberturaPrepare']) {
          ext.outputDir = "${sourceSets.main.output.classesDir}-instrumented"
          doLast {
            ant.coberturaInstrument(todir: outputDir, datafile: coberturaPrepare.datafileLocation) {
              fileset(dir: sourceSets.main.output.classesDir, excludes:coberturaExcludes)
            }   
          }
        }

        project.task('coberturaCoverage', dependsOn: ['coberturaInstrument'])

        project.task('coberturaReport', dependsOn: ['coberturaPrepare','coberturaCoverage']) << {
          ant.coberturaReport(format: "html", destdir: "${reporting.baseDir}/coverage/html",
            srcdir: 'src/main/java', datafile: coberturaPrepare.datafileLocation)
          ant.coberturaReport(format: "xml", destdir: "${reporting.baseDir}/coverage/xml",
            srcdir: 'src/main/java', datafile: coberturaPrepare.datafileLocation)
        }

        apply plugin: 'sonar'

        sonar {
          server {
            url "${sonarUrl}"
          }
          database {
            url "${sonarDatabaseUrl}"
            driverClassName 'com.mysql.jdbc.Driver'
            username "${sonarUsername}"
            password "${sonarPassword}"
          }
        }

        sonar.project.withProjectProperties { props ->
          props['sonar.profile'] = 'ClockWork'
          //Use the proper task outputDir convention
          props['sonar.cobertura.reportPath'] = "$buildDir/reports/cobertura/coverage.xml"
          props['sonar.dryRun'] = "${sonarDryRun}"
        }
        sonarAnalyze.dependsOn coberturaReport
      }
    }
  }
}
