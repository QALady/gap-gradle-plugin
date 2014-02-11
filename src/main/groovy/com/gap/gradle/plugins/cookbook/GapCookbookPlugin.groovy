package com.gap.gradle.plugins.cookbook

import org.gradle.api.Plugin
import org.gradle.api.Project

class GapCookbookPlugin implements Plugin<Project> {

    static def CONFIG_FILE = "${System.getProperty('user.home')}/.watchmen/gapcookbook.properties"

    void apply(Project project) {
        project.extensions.create('jenkins', JenkinsConfig)
        project.extensions.create('chef', ChefConfig)
		project.extensions.create('parameters', ParameterConfig)

        loadConfig(project)

        project.task('generateCookbookMetadata') << {
            new GenerateCookbookMetadataTask(project).execute()
        }

        project.task('validateCookbookDependencies', dependsOn: 'generateCookbookMetadata') << {
            new ValidateCookbookDependenciesTask(project).execute()
        }

        project.task('checkCookbookDependencies', dependsOn: [
            'validateCookbookDependencies',
        ])

        project.task('publishCookbookToArtifactory', dependsOn: [ 'generateCookbookMetadata', 'checkCookbookDependencies' ]) << {
            new PublishCookbookToArtifactoryTask(project).execute()
        }

        project.task('publishCookbookToChefServer', dependsOn: [ 'generateCookbookMetadata', 'checkCookbookDependencies' ]) << {
            new PublishCookbookToChefServerTask(project).execute()
        }
		project.task('promoteChefObjectsToServer') {
			doLast {
				new PromoteChefObjectsToServerTask(project).execute()
			}
		}
    }

    def loadConfig(Project project) {
        File configFile = new File(CONFIG_FILE)
        if (configFile.exists()) {
            Properties properties = new Properties()
            properties.load(new InputStreamReader(new FileInputStream(configFile)))
            properties.each {
                setConfigProperty(project, it.key, it.value)
            }
        }
    }

    /**
     * Sets value of config property by walking object graph to the leaf property.
     *
     * <p>Parameter {@code name} is a dot-separated name, such as {@code "jenkins.serverUrl"}. This
     * method walks from the root {@link Project project} object to the target leaf property
     * {@code serverUrl} and then assigns the {@code value} to it.</p>
     *
     * @param project The gradle project
     * @param name The dot-separated config name (i.e., jenkins.serverUrl)
     * @param value The value of the config property
     */
    def setConfigProperty(project, name, value) {
        def segments = name.split('\\.')
        def target = project
        // walk until the leaf property
        for (int i = 0; i < segments.size() - 1; i++) {
            target = target."${segments[i]}"
        }
        // set value on leaf property
        target."${segments.last()}" = value
    }

}
