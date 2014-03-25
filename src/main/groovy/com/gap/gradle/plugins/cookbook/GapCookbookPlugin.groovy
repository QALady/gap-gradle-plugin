package com.gap.gradle.plugins.cookbook

import com.gap.gradle.utils.ConfigUtil
import org.gradle.api.Plugin
import org.gradle.api.Project

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

        project.task('validateCookbook', dependsOn: ['validateCookbookDependencies', 'validateTransitiveCookbookDependencies'])

        project.task('publishCookbookToArtifactory', dependsOn: [ 'generateCookbookMetadata', 'validateCookbook' ]) << {
            new PublishCookbookToArtifactoryTask(project).execute()
        }

        project.task('publishCookbookToChefServer', dependsOn: [ 'generateCookbookMetadata', 'validateCookbook' ] ) << {
            new PublishCookbookToChefServerTask(project).execute()
        }
    }
}
