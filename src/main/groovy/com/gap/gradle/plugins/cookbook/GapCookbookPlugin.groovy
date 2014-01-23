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

        project.task('publishCookbookToChefServer') <<{

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
            Properties credentials = new Properties()
            credentials.load(new InputStreamReader(new FileInputStream(configFile)))
            credentials.each {
                setConfig(project, it.key, it.value)
            }
        }
    }

    def setConfig(project, name, value) {
        def parts = name.split('\\.')
        def target = project
        for (int i = 0; i < parts.size() - 1; i++) {
            target = target."${parts[i]}"
        }
        target."${parts[parts.size() - 1]}" = value
    }

    void verifyIfCookbookDirectoryIsValid() {
        def current_dir = System.getProperty("user.dir")
        def pattern = ".*/cookbooks/[^/]+"
        if ( !(current_dir ==~ pattern)) {
            throw new Exception("Your current working directory is ${current_dir}. However for uploading to chef server your cookbook should be located in 'cookbooks/<your cookbook name>'")
        }
    }


}
