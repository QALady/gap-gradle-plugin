package com.gap.gradle.plugins

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*
import static org.mockito.Mockito.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import com.gap.gradle.extensions.WatchmenDSLExtension

class GapWMSegmentDslPluginTest {
	private Project project
	WatchmenDSLExtension extension

	@Before
	void setup() {
		project = ProjectBuilder.builder().build()
		project.apply plugin: 'gap-wm-segmentdsl'
	}

	@Test
	void shouldExistTaskCreateECProcedure() {
		taskShouldExist('createECProcedure', project)
	}

	static def taskShouldExist(task, project) {
		assertThat("Task '${task}' does not exist on project", project.tasks.findByName(task), notNullValue())
	}
}
