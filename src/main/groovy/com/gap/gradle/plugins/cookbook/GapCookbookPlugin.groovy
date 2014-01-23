package com.gap.gradle.plugins.cookbook

import org.gradle.api.Plugin
import org.gradle.api.Project

class GapCookbookPlugin implements Plugin<Project> {

    static def CONFIG_FILE = "${System.getProperty('user.home')}/.watchmen/gapcookbook.properties"
        
    void apply(Project project) {
        project.extensions.create('jenkins', JenkinsConfig)
        project.extensions.create('chef', ChefConfig)

        loadConfig(project)

        project.task('publishCookbookToArtifactory') << {
            new PublishCookbookToArtifactoryTask(project).execute()
        }

        project.task('publishCookbookToChefServer2') << {
            new PublishCookbookToChefServerTask(project).execute()
        }

        project.task('publishCookbookToChefServer') << {

            verifyIfCookbookDirectoryIsValid()

            def home_dir = System.getenv()['HOME']
            def current_dir = System.getProperty("user.dir")
            def knife_push_working_dir = "${current_dir}/../.."
            project.exec {
                workingDir knife_push_working_dir
                commandLine "${home_dir}/knife/push.rb", '.'
            }
        }
    }

    def loadConfig(Project project) {
        File configFile = new File(CONFIG_FILE)
        if (configFile.exists()) {
            Properties properties = new Properties()
            properties.load(new InputStreamReader(new FileInputStream(configFile)))
            properties.each {
                setConfig(project, it.key, it.value)
            }
        }
    }

    def setConfig(project, name, value) {
        def segments = name.split('\\.')
        def target = project
        for (int i = 0; i < segments.size() - 1; i++) {
            target = target."${segments[i]}"
        }
        target."${segments[segments.size() - 1]}" = value
    }

    def verifyIfCookbookDirectoryIsValid() {
        def current_dir = System.getProperty("user.dir")
        def pattern = ".*/cookbooks/[^/]+"
        if (!(current_dir ==~ pattern)) {
            throw new Exception("Your current working directory is ${current_dir}. However for uploading to chef server your cookbook should be located in 'cookbooks/<your cookbook name>'")
        }
    }
}
