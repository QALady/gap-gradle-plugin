package com.gap.gradle.tasks

import org.gradle.api.Project

import com.gap.gradle.utils.ShellCommand
import com.gap.gradle.utils.ShellCommandException
import com.gap.pipeline.ec.Property
import com.gap.pipeline.tasks.WatchmenTask

class CopyUpStreamChangeLogDownStreamTask extends WatchmenTask {
	Project project
	ShellCommand shellCommand = new ShellCommand()

	CopyUpStreamChangeLogDownStreamTask(Project project) {
		super(project)
		this.project = project
	}

	def execute() {
		validate()
		//ectool getProperties --path /myProcedure/testsheet --recurse 1
		def prop
		try{
			prop = shellCommand.execute(['ectool', 'getProperty', '--path', '/myProcedure/testsheet', '--recurse', '1'])
		  }
		  catch (ShellCommandException e) {
			if(e.message.contains('[NoSuchProperty]')){
			  logger.debug("Requested property does not exist. ${e.message}\n")
			  return Property.invalidProperty(key)
			}
			else throw e
		  }
	  
		  println prop
		 def data = new XmlSlurper().parseText(prop)
		 data.property.each { p ->
			 println p.propertyId
			 println p.propertyName
			 println p.value
		 }
	}
	
}
