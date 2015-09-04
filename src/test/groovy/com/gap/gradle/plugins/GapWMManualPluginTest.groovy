package com.gap.gradle.plugins

import org.junit.Ignore

import static org.junit.Assert.*
import static org.hamcrest.Matchers.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

@Ignore
class GapWMManualPluginTest {
	Project project
	
	@Before
	void setup() {
		project = ProjectBuilder.builder().build();
		project.apply plugin: 'gap-wm-manual'
	}
	
	@Test
	void shouldExistTaskUploadGradleWithSelectedDependencyVersions() {
		taskShouldExist('uploadGradleWithSelectedDependencyVersions', project)
	}
	
	static def taskShouldExist(task, project) {
		assertThat("Task '${task}' does not exist on project", project.tasks.findByName(task), notNullValue())
	}

	@Test
	void shouldExistTaskCreateHtmlWithGoodVersions() {
		taskShouldExist('createHtmlWithGoodVersions', project)
	}

}
