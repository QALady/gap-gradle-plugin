package com.gap.gradle.plugins.openstack

import com.gap.gradle.plugins.cookbook.JenkinsConfig
import com.gap.gradle.utils.ConfigUtil
import org.gradle.api.Plugin
import org.gradle.api.Project

class OpenStackCleanUpPlugin implements Plugin<Project> {

    static def CONFIG_FILE = "${System.getProperty('user.home')}/.watchmen/gapchef.properties"

    void apply(Project project) {
        project.extensions.create('jenkins', JenkinsConfig)


        new ConfigUtil().loadConfig(project, CONFIG_FILE)

        project.task('cleanUpOrphanObjects') << {
            new CleanUpOrphanObjectsTask(project).execute()
        }
    }
}
