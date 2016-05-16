package com.gap.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.gap.pipeline.ec.CommanderClient
import com.gap.gradle.utils.Constants.*

import org.apache.commons.logging.LogFactory

class GapWMExperimentsPlugin implements Plugin<Project> {

    private static final logger = LogFactory.getLog(GapWMExperimentsPlugin)

  CommanderClient ecclient = new CommanderClient()

  @Override
  public void apply(Project project) {

      project.repositories {
          ivy {
            name "wm_local_non_prod"
            layout "maven"
            url "http://artifactory.gapinc.dev/artifactory/local-non-prod"
            credentials {
              username "${ecclient.getArtifactoryUserName()}"
              password "${ecclient.getArtifactoryPassword()}"
            }
          }
          maven {
              name "wm_maven_remote_repos"
              url "http://artifactory.gapinc.dev/artifactory/maven-repos"
          }
          ivy {
              name "wm_ivy_remote_repos"
              layout "maven"
              url "http://artifactory.gapinc.dev/artifactory/maven-repos"
          }
      }

      project.wrapper {
        distributionUrl "http://artifactory.gapinc.dev/artifactory/gradle-${gradleVersion}-bin.zip"
      }

      project.task('testCredentials') <<{
           def userName = ecclient.getArtifactoryUserName()
           def password = ecclient.getArtifactoryPassword()

           logger.info(userName)
           logger.info(password)
      }
  }
}
