package com.gap.gradle.plugins

import com.gap.gradle.ivy.IvyInfo
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.gap.pipeline.ec.CommanderClient

class GapWMExperimentsPlugin implements Plugin<Project> {

  CommanderClient ecclient = new CommanderClient()

  void apply(Project project) {

      project.repositories {
          ivy {
            name "wm_local_non_prod"
            layout "maven"
            url "http://artifactory.gapinc.dev/artifactory/local-non-prod"
            credentials {
              username "$ecclient.getArtifactoryUserName()"
              password "$ecclient.getArtifactoryPassword()"
            }
          }
          maven {
              name "wm_maven_remote_repos"
              url "http://artifactory.gapinc.dev/artifactory/remote-repos"
          }
          ivy {
              name "wm_ivy_remote_repos"
              layout "maven"
              url "http://artifactory.gapinc.dev/artifactory/remote-repos"
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
