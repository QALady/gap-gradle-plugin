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
        def path = new File(project.chef.cookbookDir, "berks")
        createBerksfile(path)
        downloadDependencies(path)
        verifyDependencies(path)
    }

    def verifyDependencies(File path) {
        def command = new ShellCommand(path)
        def missing = []
        try {
            command.execute("switchblade prod")
            readSources(path).each { name, info ->
                def versions = command.execute("knife cookbook show ${name}").split(/\s+/)
                if (!versions.contains(info["locked_version"])) {
                    missing << "${name}@${info["locked_version"]} (latest is ${versions[1]})"
                }
            }
        } finally {
            command.execute("switchblade tdev")
        }
        if (!missing.isEmpty()) {
            throw new UnpinnedDependencyException("These transitive dependencies don't exist in prod: ${missing.join(', ')}")
        }
    }

    def readSources(File path) {
        return new JsonSlurper().parse(new FileReader(new File(path, "Berksfile.lock")))["sources"]
    }

    def downloadDependencies(File path) {
        def command = new ShellCommand(path)
        command.execute("switchblade tdev")
        command.execute("berks install", envFor(path))
    }

    def createBerksfile(File path) {
        path.mkdirs()
        def berksfile = new File(path, "Berksfile")
        berksfile.append("chef_api :config\n")
        project.chef.metadata.dependencies.each { cookbook, version ->
            berksfile.append("cookbook '${cookbook}', '${version}'\n")
        }
    }

    def envFor(path) {
        def env = ["BERKSHELF_PATH=${path}/cookbooks"]
        System.getenv().each { k, v -> env.add("$k=$v") }
        env
    }
}
