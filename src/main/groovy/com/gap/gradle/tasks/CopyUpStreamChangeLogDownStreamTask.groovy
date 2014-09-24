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
			prop = shellCommand.execute(['ectool', 'getProperties', '--path', '/myJob/ecscm_changeLogs', '--recurse', '1'])
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
		 println "Data after xml slurping..."
		 println data
		 println "response object...."
		 println data.response
		 println "propertySheet object in response..."
		 println data.response.propertySheet
		 println "propertySheet object in data..."
		 println data.propertySheet
		 println "property in data"
		 println data.property
		 println "property in propertySheet..."
		 println data.propertySheet.property
		 println "printing each prop..."
		 data.data.response.propertySheet.property.each { p ->
			 println p.propertyId
			 println p.propertyName
			 println p.value
		 }
	}
	
}
