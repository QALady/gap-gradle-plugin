package com.gap.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import com.gap.gradle.tasks.CreateHtmlWithGoodVersionsTask

//import com.gap.gradle.ivy.IvyInfo
//import com.gap.gradle.utils.ShellCommand
//import com.gap.gradle.utils.ShellCommandException
//import com.gap.pipeline.ec.CommanderClient

class GapWMManualPlugin implements Plugin<Project> {

  //CommanderClient ecclient = new CommanderClient()
  //ShellCommand shellCommand = new ShellCommand()
  //def ivyInfo = new IvyInfo(project)
  //def dependenciesWMMAN = ""
  //SegmentRegistry segmentRegistry = new SegmentRegistry()

  
  void apply(Project project) {

  		project.task('createHtmlWithGoodVersions') << {
            new CreateHtmlWithGoodVersionsTask(project).execute()
        }
 }
}

