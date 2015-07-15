package com.gap.gradle.plugins.resources

import org.apache.commons.logging.LogFactory
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.util.regex.Pattern

class GapResourcesPlugin implements Plugin<Project>{

    def logger = LogFactory.getLog(GapResourcesPlugin)

  void apply(Project target) {
    target.configure(target) {

      evaluationDependsOnChildren()
      target.extensions.create('templates', GapResourcesExtension, "**/*.erb", ".erb" ,".rb")

      if (target.plugins.hasPlugin('java')) {
        sourceSets.all { sourceSet ->
          def taskName = sourceSet.getTaskName('process', 'Resources')

          configure(project.getTasksByName(taskName, false)) {
            from sourceSet.allSource
            exclude sourceSet.java.filter.includes
            exclude project.templates.filePattern
            exclude "${sourceSet.name}${project.templates.dataExt}"

            //eachFile {println it.path}
            dependsOn sourceSet.getTaskName('process', 'Templates')
          }
        }

        configurations {
          erubis {visible false}
        }

        dependencies {
          erubis "com.gap.gradle:process-erb-templates:0.0.1"
        }

        sourceSets.all { sourceSet ->
          def taskName = sourceSet.getTaskName('process', 'Templates')
          def resourcesOutPath = "$buildDir/templates/${sourceSet.name}"

          def processTemplatesTask = project.tasks.create(name: taskName, description: "Processes the ${sourceSet.name} erb templates.") {
            new File(resourcesOutPath).mkdirs()
            inputs.files sourceSet.resources, sourceSets.main.resources
            outputs.files fileTree(resourcesOutPath) { exclude project.templates.filePattern }
          }

          sourceSet.runtimeClasspath += files(resourcesOutPath)

          processTemplatesTask << {
            def resourcesFolder = fileTree(resourcesOutPath);
            delete(resourcesOutPath)

            project.copy {
              from sourceSets.main.resources
              from sourceSet.resources
              into resourcesOutPath
              include project.templates.filePattern
              include "${sourceSet.name}${project.templates.dataExt}"
              include "${sourceSets.main.name}${project.templates.dataExt}"
              //eachFile {println it.path }
            }

            def erbs = resourcesFolder.matching {
              include project.templates.filePattern
            }

            def dataMainSet = resourcesFolder.matching {
              include "${sourceSets.main.name}${project.templates.dataExt}"
            }

            def dataCurrentSet = resourcesFolder.matching {
              include "${sourceSet.name}${project.templates.dataExt}"
            }

              //this.processErbs(erbs, project, configurations, dataMainSet, dataCurrentSet)
              //process Erbs

              erbs.each { erbFile ->
                  def processedFile = (erbFile.path =~ Pattern.compile(project.templates.fileExt + '$')).replaceFirst('')
                  logger.debug "$erbFile --> $processedFile"
                  processedFile

                  exec {
                      //excutable "/usr/bin/erubis" if gem is installed
                      executable "java"
                      args "-jar", configurations.erubis.asPath, "-f", dataMainSet.files.join(',') + ',' + dataCurrentSet.files.join(','), erbFile
                      standardOutput = new BufferedOutputStream(new FileOutputStream(processedFile))
                  }

                  delete(erbFile)
              }

          }
        }
      }
    }
  }
}
