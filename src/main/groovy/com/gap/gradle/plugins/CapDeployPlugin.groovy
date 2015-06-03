package com.gap.gradle.plugins

import groovy.json.JsonSlurper
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.apache.commons.logging.LogFactory

class CapDeployPlugin implements Plugin<Project> {

    def logger = LogFactory.getLog(CapDeployPlugin)

    void apply(Project project) {
        project.extensions.create('deploy', CapDeployExtension)
        project.task ('deploy') << {
            def deployVersions = [:]
            def dependencies = project.configurations[project.deploy.configuration].resolve()
            project.deploy.components.each { component, configuration ->
                deployVersions[component] = [:]
                configuration.each { segment, coords ->
                    def versions = gatherConfiguration(project, coords, dependencies)
                    if (versions.isEmpty()) {
                        logger.info("No version information found for component '${component}:${segment}'")
                    } else {
                        deployVersions[component][segment] = versions
                    }
                }
            }
            reportDeployVersions(deployVersions)
            deployComponents(project, deployVersions)
        }
    }

    def deployComponents(Project project, components) {
        for (entry in components) {
            def name = entry.key
            def component = entry.value
            if (component.containsKey('capfiles')) {
                project.copy {
                    from project.zipTree(component.capfiles.file)
                    into "${name}-capfiles"
                }
                logger.info("Deploying ${name}...")
                def builder = new ProcessBuilder()
                builder.redirectErrorStream(true)
                builder.command(
                    'cap', "${project.deploy.configuration}", 'deploy_app',
                    '-s', "version=${component.archives.version}",
                    '-s', "appConfig=${project.deploy.configuration}",
                    '-s', "cookbook_version=${component.cookbooks.version}"
                )
                builder.directory(project.file("${name}-capfiles/deploy"))
                def proc = builder.start()
                proc.waitFor()
                if (proc.exitValue() == 0) {
                    logger.info("Deployment result for ${name}:")
                    proc.inputStream.eachLine { logger.info(it) }
                } else {
                    logger.error("Error occured while deploying ${name}:")
                    proc.inputStream.eachLine { logger.info(it) }
                    throw new GradleException("Halting deployment!")
                }
            } else {
                logger.info("No capfiles found for ${name}")
            }
        }
    }

    def reportDeployVersions(deployVersions) {
        logger.info("Deploy versions: ")
        deployVersions.each { name, component ->
            logger.info("\t${name} component:")
            component.each { k, v ->
                logger.info("\t\t${k} version: ${v.version}")
                if (k == 'capfiles') {
                    logger.info("\t\tcapfiles archive: ${v.file}")
                }
            }
        }
    }

    def gatherConfiguration(Project project, coords, dependencies) {
        for (dependency in dependencies) {
            def (group, name, ext) = coords.split(':')
            def path = dependency.absolutePath
            if (path.contains("/${group}/${name}/") && path.endsWith(ext)) {
                def version
								if (dependency.name.startsWith("metadata-") && dependency.name.endsWith(".json")) {
   									def json = new JsonSlurper().parse(new FileReader(project.file(path)))
										version = json.version
								} else {
										version = path.substring(path.lastIndexOf('-') + 1, path.lastIndexOf('.'))
                }
                return [
                    version: version,
										file: project.file(path),
                ]
            }
        }
        return [:]
    }
}
