package com.gap.gradle.plugins.gapcookbook
import com.gap.gradle.plugins.JenkinsConfig
import org.gradle.api.Plugin
import org.gradle.api.Project

class GapCookbookPlugin implements Plugin<Project> {
        
    void apply(Project project) {
        project.extensions.create('jenkins', JenkinsConfig)
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

    void verifyIfCookbookDirectoryIsValid() {
        def current_dir = System.getProperty("user.dir")
        def pattern = ".*/cookbooks/[^/]+"
        if ( !(current_dir ==~ pattern)) {
            throw new Exception("Your current working directory is ${current_dir}. However for uploading to chef server your cookbook should be located in 'cookbooks/<your cookbook name>'")
        }
    }


}
