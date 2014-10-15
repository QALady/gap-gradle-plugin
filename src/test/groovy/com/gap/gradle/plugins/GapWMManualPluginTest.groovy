package com.gap.gradle.plugins

import static org.junit.Assert.*
import static org.hamcrest.Matchers.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class GapWMManualPluginTest {
	Project project
	
	@Before
	void setup() {
		project = ProjectBuilder.builder().build();
		project.apply plugin: 'gapmanual'
	}
	
	@Test
	void shouldExistTaskUploadGradleWithSelectedDependencyVersions() {
		taskShouldExist('uploadGradleWithSelectedDependencyVersions', project)
	}
	
	static def taskShouldExist(task, project) {
		assertThat("Task '${task}' does not exist on project", project.tasks.findByName(task), notNullValue())
	}

}
