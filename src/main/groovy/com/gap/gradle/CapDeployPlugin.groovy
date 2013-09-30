package com.gap.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.GradleException

class CapDeployPlugin implements Plugin<Project> {

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
                        println("No version information found for component '${component}:${segment}'")
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
                println "Deploying ${name}..."
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
                    println "Deployment result for ${name}:"
                    proc.inputStream.eachLine { println it }
                } else {
                    println "Error occured while deploying ${name}:"
                    proc.inputStream.eachLine { println it }
                    println()
                    throw new GradleException("Halting deployment!")
                }
            } else {
                println "No capfiles found for ${name}"
            }
            println()
        }
    }

    def reportDeployVersions(deployVersions) {
        println "Deploy versions: "
        deployVersions.each { name, component ->
            println "\t${name} component:"
            component.each { k, v ->
                println "\t\t${k} version: ${v.version}"
                if (k == 'capfiles') {
                    println "\t\tcapfiles archive: ${v.file}"
                }
            }
            println()
        }
    }

    def gatherConfiguration(Project project, coords, dependencies) {
        for (dependency in dependencies) {
            def (group, name, ext) = coords.split(':')
            def path = dependency.absolutePath
            if (path.contains("/${group}/${name}/") && path.endsWith(ext)) {
                return [
                    version: path.substring(path.lastIndexOf('-') + 1, path.lastIndexOf('.')),
                    file: project.file(path),
                ]
            }
        }
        return [:]
    }
}
