package com.gap.gradle.plugins

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class GapAntHelperPluginTest {

	private Project project
	private static String pluginName = 'gapant'

	@Before
	void setup() {
		project = ProjectBuilder.builder().build()
		project.apply plugin: pluginName
	}

	@Test
	void addAthenaDepsToAntPathTaskIsAddedToProject(){
		taskShouldExist('addDependencyConfigToAntPath')
	}

	def taskShouldExist(task) {
		assertThat(project.tasks.findByName(task), notNullValue())
	}

}
