

package com.gap.gradle.plugins

import com.gap.gradle.ivy.IvyInfo
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.gap.pipeline.ec.CommanderClient

import com.gap.gradle.utils.ShellCommand

class GapWMExperimentsPlugin implements Plugin<Project> {

  CommanderClient ecclient = new CommanderClient()
  def ivyInfo = new IvyInfo(project)
  def dependenciesWMMAN = ""

  void apply(Project project) {

      project.task('WMManualIvyDependencies') <<{
           
          try {
            shellCommand.execute(["ectool", "getFullCredential", """/projects/WM Credentials/credentials/WMArtifactory""", "--value", """password"""])
          } catch(ShellCommandException se) {
            if(se.getMessage().contains('ectool error [InvalidCredentialName]')) {
            logger.warn("WARNING: Using dummy credentials - Only WM Gradle:Invoke & WM Exec:Run are approved steps to access artifactory credentials. This will not impact your job unless you are trying to use the Artifactory credentials in this step")
            return "dummy"
            } else {
              throw se
            }
          }

          if(isRootProject(project)){
                    ivyInfo.dependencies().each {dependenciesWMMAN = dependenciesWMMAN + it}
          }
      }
        

      project.task('testCredentials') <<{
           def userName = ecclient.getArtifactoryUserName()
           def password = ecclient.getArtifactoryPassword()

           println userName
           println password
      }
  }
}
