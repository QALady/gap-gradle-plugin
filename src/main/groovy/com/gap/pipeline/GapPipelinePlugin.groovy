package com.gap.pipeline
import groovy.json.JsonSlurper

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.gap.gradle.ivy.IvyInfo
import com.gap.gradle.tasks.CopyUpStreamChangeLogDownStreamTask
import com.gap.gradle.tasks.GetResolvedVersionTask
import com.gap.gradle.tasks.PromoteArtifactsTask
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.tasks.*
//CookbookConfig cookbookDetail

class GapPipelinePlugin implements Plugin<Project> {

    CommanderClient ecclient = new CommanderClient()

    void apply(Project project) {
        project.extensions.create('prodPrepare', com.gap.pipeline.ProdPrepareConfig)
        project.extensions.create('ivy', IvyConfig)

        loadProperties(project, "prodPrepare", "ivy")

        configureTasksRequiredByWatchmenSegment(project)

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
              url "http://artifactory.gapinc.dev/artifactory/remote-repos"
          }
          ivy {
              name "wm_ivy_remote_repos"
              layout "maven"
              url "http://artifactory.gapinc.dev/artifactory/remote-repos"
          }
        }

        project.task('setupBuildDirectories') <<{
            new SetUpBuildDirectoriesTask(project).execute()
        }

        project.task('validatePrepareForProductionInput') <<{
            new PrepareForProductionDeployTask(project).validate()
        }

        project.task('prepareForProductionDeploy', dependsOn: ['setupBuildDirectories','validatePrepareForProductionInput', 'generateChangeListReport']) << {
            new PrepareForProductionDeployTask(project).execute()
        }

        project.task('uploadBuildArtifacts') << {
            new UploadBuildArtifactsTask(project).execute()
        }

        if(project.getGradle().startParameter.taskNames.contains('downloadArtifacts')){
            project = new DownloadArtifactsTask(project).configure()
        }

        if(project.getGradle().startParameter.taskNames.contains('promoteArtifacts')){
            project = new PromoteArtifactsTask(project).configure()
        }

        project.task('downloadArtifacts') << {
            new DownloadArtifactsTask(project).execute()
        }

        project.task('generateChangeListReport',dependsOn: ['setupBuildDirectories']) << {
            new GenerateChangeListReportTask(project).execute()
        }

        project.task("promoteArtifacts") << {
            new PromoteArtifactsTask(project).execute()
        }

        project.task("getResolvedVersion") << {
            new GetResolvedVersionTask(project).execute()
        }

        // Apply this to get rid of diamond dependency issue between components
        /*
        if(project.hasProperty("failOnVersionConflict")) {
            project.configurations.all {
                resolutionStrategy.failOnVersionConflict()
            }
        }
        */

        project.task('createECLinks') << {
            new CreateECLinksTask(project).execute()
        }


        project.task('buildJsonWithAllResolvedVersions') << {
            new BuildJsonWithAllResolvedVersionsTask(project).execute()
        }
    }

    private configureTasksRequiredByWatchmenSegment(Project project) {
        //changing the following code might have undesired side effects.... used by WM Segment pipeline
        project.configure(project) {
            def ivyInfo = new IvyInfo(project)

            project.task('ivyIdentifiers') << {
                if(isRootProject(project)){
                    ivyInfo.identifiers().each {println it}
                }
            }

            project.task('ivyDependencies') << {
                if(isRootProject(project)){
                    ivyInfo.dependencies().each {println it}
                }
            }

            project.task('ivySegmentVersion') << {
                println ivyInfo.version()
            }

            project.task('populateSegmentRegistry') << {
                if(isRootProject(project)){
                    new PopulateSegmentRegistryTask(project).execute()
                }
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

            project.task('resolveCookbookShaId') << {
                getCookbookDetails(project.configurations).each() { name, sha1ID -> println name + "," + sha1ID}
            }

			project.task('copyUpstreamChangelogDownstream') << {
				// ITCI-1170: TODO implement copy of upstream changelog to downstream.
				new CopyUpStreamChangeLogDownStreamTask(project).execute()
			}

        }

    }

    private boolean isRootProject(def project) {
        project.equals(project.rootProject)
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

    def getCookbookDetails(configurations) {
        def cookbookInfo = [:]
        def name = ""
        def sha1ID = ""
        if (configurations.hasProperty('cookbooks')) {
            configurations.cookbooks.resolvedConfiguration.resolvedArtifacts.each { artifact ->
                if (artifact.file.path =~ /\.txt$/) {
                    String fileContents = new File(artifact.file.path).text
                    def cookbookDetails = fileContents.split ("\n")
                    sha1ID = cookbookDetails[0]
                }
                if (artifact.file.path =~ /\.json$/) {
                    def json = new JsonSlurper().parse(new FileReader(artifact.file.path))
                    name = json.name
                }
                if(name != "" && sha1ID != "") {
                    cookbookInfo[name] = sha1ID
                    name = ""
                    sha1ID = ""
                }
            }
        }
        return cookbookInfo
    }
}
