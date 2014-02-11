package com.gap.gradle.plugins.cookbook

import com.gap.gradle.utils.ShellCommand
import groovy.json.JsonSlurper
import org.gradle.api.Project

class ValidateTransitiveCookbookDependenciesTask {

    Project project

    ValidateTransitiveCookbookDependenciesTask(Project project) {
        this.project = project
    }

    def execute() {
        if (project.chef.requireTransitiveDependencies) {
            requireMetadata()
            verifyDependencies()
        }
    }

    def requireMetadata() {
        if (!project.chef.metadata) {
            throw new IllegalStateException("No metadata found on project!")
        }
    }

    def verifyDependencies() {
        project.chef.metadata.dependencies.each { cookbook, version ->
            def path = pathFor(cookbook, version)
            def command = new ShellCommand(path)
            command.execute("berks install", envFor(path))
            new JsonSlurper().parse(new FileReader(new File(path, "Berksfile.lock")))["sources"].each { name, info ->
                // TODO test for existence in prod
                println "==================================== ${name} '${info["locked_version"]}'"
            }
        }
    }

    def envFor(path) {
        def env = ["BERKSHELF_PATH=${path}/berkshelf"]
        System.getenv().each { k, v -> env.add("$k=$v") }
        env
    }

    def pathFor(cookbook, version) {
        def path = new File(project.chef.cookbookDir, "${cookbook}-${version}")
        path.mkdirs()
        new File(path, "Berksfile").write([
            "chef_api :config",
            "cookbook '${cookbook}', '${version}'"
        ].join('\n'))
        return path
    }
}
