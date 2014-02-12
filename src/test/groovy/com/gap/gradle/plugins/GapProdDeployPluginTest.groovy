package com.gap.gradle.plugins

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class GapProdDeployPluginTest {
		
	private Project project
	
	@Before
	void setup() {
		project = ProjectBuilder.builder().build()
		project.apply plugin: 'gapproddeploy'
	}
	
	@Test
	void triggerProdDeployTaskIsAddedToProject(){
		taskShouldExist('deployToProduction')
	}

	def taskShouldExist(task) {
		assertThat(project.tasks.findByName(task), notNullValue())
	}
}
