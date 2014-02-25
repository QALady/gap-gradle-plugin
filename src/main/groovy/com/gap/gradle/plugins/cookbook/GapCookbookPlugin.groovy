package com.gap.gradle.plugins.cookbook

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.gap.gradle.utils.ConfigUtil

class GapCookbookPlugin implements Plugin<Project> {

    static def CONFIG_FILE = "${System.getProperty('user.home')}/.watchmen/gapcookbook.properties"

    void apply(Project project) {
        project.extensions.create('jenkins', JenkinsConfig)
        project.extensions.create('chef', ChefConfig)

        new ConfigUtil().loadConfig(project, CONFIG_FILE)

        project.task('generateCookbookMetadata') << {
            new GenerateCookbookMetadataTask(project).execute()
        }

        project.task('validateCookbookDependencies', dependsOn: 'generateCookbookMetadata') << {
            new ValidateCookbookDependenciesTask(project).execute()
        }

        project.task('validateTransitiveCookbookDependencies', dependsOn: [ 'generateCookbookMetadata', 'validateCookbookDependencies' ]) << {
            new ValidateTransitiveCookbookDependenciesTask(project).execute()
        }

        project.task('publishCookbookToArtifactory', dependsOn: [ 'generateCookbookMetadata', 'validateCookbookDependencies' ]) << {
            new PublishCookbookToArtifactoryTask(project).execute()
        }

        project.task('publishCookbookToChefServer', dependsOn: [ 'generateCookbookMetadata', 'validateCookbookDependencies' ] ) << {
            new PublishCookbookToChefServerTask(project).execute()
        }
    }
}
