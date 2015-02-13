package com.gap.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator
import org.gradle.api.tasks.JavaExec

import javax.inject.Inject

class DbotPlugin implements Plugin<Project> {

    private final Instantiator instantiator
    private Project project

    @Inject
    public DbotPlugin(Instantiator instantiator) {
        this.instantiator = instantiator
    }

    @Override
    void apply(Project project) {
        project.plugins.apply('liquibase')
        this.project = project
        project.repositories {
          add(new org.apache.ivy.plugins.resolver.URLResolver()) {
            name = 'dbot_repo'
              validate = false
              addIvyPattern("http://nfs01.sf.gid.gap.com/build_artifacts/[organisation]/[module]/ivy-[revision].xml")
              addArtifactPattern("http://nfs01.sf.gid.gap.com/build_artifacts/[organisation]/[module]/[revision]/[type]/[artifact]-[revision].[ext]")
              addArtifactPattern("http://nfs01.sf.gid.gap.com/build_artifacts/[organisation]/[module]/[revision]/[artifact]-[revision].[ext]")
              addArtifactPattern("http://nfs01.sf.gid.gap.com/build_artifacts/[organisation]/[module]/[revision]/[organisation]-[artifact]-[revision].[ext]")
          }
          maven {
            url "http://nfs01.sf.gid.gap.com/build_artifacts"
          }
          ivy {
            layout "maven"
              url "http://nfs01.sf.gid.gap.com/build_artifacts"
          }
          mavenCentral()
        }

        project.afterEvaluate {
          createTasks()
        }
    }

    void createTasks() {
        project.task("generateChangeLogDBOT", type: JavaExec) {
          description 'generateChangeLog of the database obejcts to files'
          classpath project.configurations.runtime
            main = 'com.gap.gid.dbot.Main'
            doFirst {
              args = [
                "--username=" + project.ext.username,
                "--password=" + project.ext.password,
                "--url=" + project.ext.url,
                "--schema=" + project.ext.schema,
                "--driver=" + project.ext.driver,
                "--flavor=" + project.ext.flavor,
                ].toList()
            }
        }
        project.task("runSQL", type: JavaExec) {
          description 'run a sql file in the database'
          classpath project.configurations.runtime
            main = 'com.gap.gid.dbot.tasks.RunSQL'
            doFirst {
              args = [
                "--username=" + project.ext.username,
                "--password=" + project.ext.password,
                "--url=" + project.ext.url,
                "--schema=" + project.ext.schema,
                "--driver=" + project.ext.driver,
                "--flavor=" + project.ext.flavor,
                "--fileName=" + project.ext.filename
                ].toList()
            }
        }

        project.task("searchInvalidObjects", type: JavaExec) {
          classpath project.configurations.runtime
            main = 'com.gap.gid.dbot.tasks.CompareInvalidObjects'
            doFirst {
              args = [
                "--username=" + project.ext.username,
                "--password=" + project.ext.password,
                "--url=" + project.ext.url,
                "--schema=" + project.ext.schema,
                "--driver=" + project.ext.driver,
                "--flavor=" + project.ext.flavor,
                ].toList()
            }
        }

        project.task("updateCheckingInvalidObjects", type: JavaExec, dependsOn: ['searchInvalidObjects','update']) {
          description 'update oracle database checking invalid objects'
            classpath project.configurations.runtime
            main = 'com.gap.gid.dbot.tasks.StoreCurrentInvalidObjects'
            doFirst {
              args = [
                "--username=" + project.ext.username,
                "--password=" + project.ext.password,
                "--url=" + project.ext.url,
                "--schema=" + project.ext.schema,
                "--driver=" + project.ext.driver,
                "--flavor=" + project.ext.flavor,
                ].toList()
            }
        }
        project.task("initDatabase", type: JavaExec) {
          description 'runs the sql file init.sql to initializes a database i.e. create database, user and schema'
          classpath project.configurations.runtime
            def m = project.ext.url =~ /(.*?)\/([\w\d]+$)/
            def urlAdminDB
            def dbName
            if (m.matches()) {
              urlAdminDB = m[0][1] + "/postgres"
              dbName = m[0][2]
            }
            main = 'com.gap.gid.dbot.tasks.CreateDatabase'
            doFirst {
              args = [
                "--username=" + project.ext.username,
                "--password=" + project.ext.password,
                "--url=" + urlAdminDB,
                "--driver=" + project.ext.driver,
                "--flavor=" + project.ext.flavor,
                "--dbName=" + dbName,
                "--schema=SYSTEM",
                "--fileName=./_INIT/init.sql"
                ].toList()
            }
        }
    }
}
