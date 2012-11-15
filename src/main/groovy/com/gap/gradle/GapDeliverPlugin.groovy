package com.gap.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin

/**
 * Delivers chosen artifacts for next step (packaging).
 *
 * Also: Handles transitive dependency on config artifacts,
 * by merging them to master config preserved with artifact namespace.
 *
 */
class GapDeliverPlugin implements Plugin<Project> {
  void apply(Project target) {
    target.configure(target) {

      //declare new configuration for deliverable, if not defined already
      configurations { deliverables }

      //delivers output for packaging task
      target.tasks.add(name: "deliverApp", dependsOn: configurations.deliverables) {
        ext.dropDir = "${buildDir}/drop"
        inputs.files configurations.deliverables
        outputs.files file(dropDir)
      }

      target.tasks.deliverApp << {
        //gather up the deliverable artifacts
        def deliverableArtifacts = configurations.deliverables.resolvedConfiguration.resolvedArtifacts
        def artifactsMap = deliverableArtifacts.groupBy {it.type}

        if (artifactsMap.config) {
          logger.info "Bundling configuration for this application."
          def masterConfigLocation = "${dropDir}/${project.name}-config-${project.version}.zip";

          ant.zip(destfile: masterConfigLocation) {
            //namespace and pack configuration artifacts
            artifactsMap.config.each { dep ->
              def namespace = "${dep.moduleVersion.id.group}:${dep.moduleVersion.id.name}"
              logger.info "Collecting ${namespace} into the deliverable master config."
              ant.zipfileset(prefix: namespace, src: dep.file.path)
            }
          }

          logger.info "Master configuration file: ${masterConfigLocation} created"
          deliverableArtifacts.removeAll(artifactsMap.config)
        }

        deliverableArtifacts.each {
          //Copy artifacts to the output directory
          logger.info "Delivering: ${it.file.name}"
          ant.copy(file: it.file.path, toDir: dropDir)
        }

        logger.info "Done delivering application to ${dropDir}"
      }

    }
  }
}
