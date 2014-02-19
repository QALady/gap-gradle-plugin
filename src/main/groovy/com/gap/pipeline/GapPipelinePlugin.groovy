package com.gap.pipeline

import com.gap.pipeline.tasks.*
import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project

class GapPipelinePlugin implements Plugin<Project> {

    void apply(Project project) {
        project.extensions.create('prodPrepare', com.gap.pipeline.ProdPrepareConfig)
        project.extensions.create('ivy', IvyConfig)
        project.extensions.create('rpmConfig', com.gap.pipeline.RpmConfig)


        loadProperties(project, "prodPrepare", "ivy", "rpmConfig")

        configureTasksRequiredByWatchmenSegment(project)

        project.task('setupBuildDirectories') <<{
            new SetUpBuildDirectoriesTask(project).execute()
        }

        project.task('validatePrepareForProductionInput') <<{
            new PrepareForProductionDeployTask(project).validate()
        }

        project.task('prepareForProductionDeploy', dependsOn: ['setupBuildDirectories','validatePrepareForProductionInput', 'generateChangeList']) << {
            new PrepareForProductionDeployTask(project).execute()
        }

        project.task('generateChangeList') << {
            new GenerateChangeListTask().execute()
        }

        project.task('uploadBuildArtifacts') << {
            new UploadBuildArtifactsTask(project).execute()
        }

        if(project.getGradle().startParameter.taskNames.contains('downloadArtifacts')){
            project = new DownloadArtifactsTask(project).configure()
        }

        project.task('downloadArtifacts') << {
            new DownloadArtifactsTask(project).execute()
        }

    }

    private configureTasksRequiredByWatchmenSegment(Project project) {
        //changing the following code might have undesired side effects.... used by WM Segment pipeline
        project.configure(project) {
            project.task('ivyIdentifiers') << {
                println project.group + ":" + project.name
            }

            project.task('ivyDependencies') << {
                configurations.each {
                    config ->
                        config.dependencies.each {
                            dep -> println dep.group + ":" + dep.name
                        }
                }
            }

            project.task('ivySegmentVersion') << {
                println project.version
            }

            project.task('unzipIntegrationTests') << {
                configurations.integrationTest.files.each {
                    file ->
                        copy {
                            from zipTree(file.path)
                            into '.'
                        }
                }
            }

            project.task('unzipSmokeTests') << {
                configurations.smokeTest.files.each {
                    file ->
                        copy {
                            from zipTree(file.path)
                            into '.'
                        }
                }
            }


            project.task('resolveCookbookDependencies') << {
                getCookbookVersions(project.configurations).each() { name, version -> println name + "," + version }
            }

        }

    }

    //we have to do this as gradle does not allowing setting extension properties from the command line.
    //gradle only sets the properties as a string and the following code parses the string and sets the extension properties.
    def loadProperties(project, String[] configKeys) {
        for (def property : project.properties) {
            configKeys.each {key ->
                if (property.key.startsWith("${key}.")) {
                    setConfigProperty(project, property.key, property.value)
                }
            }
        }
    }

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

    def getCookbookVersions(configurations) {
        def versions = [:]
        if (configurations.hasProperty('cookbooks')) {
            configurations.cookbooks.resolvedConfiguration.resolvedArtifacts.each { artifact ->
                // Skip non-json files
                if (!(artifact.file.path =~ /\.json$/)) {
                    return
                }
                def json = new JsonSlurper().parse(new FileReader(artifact.file.path))

                versions[json.name] = json.version
            }
        }
        return versions
    }

}

