package com.gap.gradle.plugins

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*
import static org.mockito.Mockito.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import com.gap.gradle.extensions.GapWMSegmentDsl

class GapWMSegmentDslPluginTest {
	private Project project
	GapWMSegmentDsl extension
	
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder()

	@Before
	void setup() {
		project = ProjectBuilder.builder().build()
		project.apply plugin: 'gap-wm-segmentdsl'
	}

	@Test
	void shouldExistTaskCreateECProcedure() {
		taskShouldExist('createECProcedure', project)
	}

	@Test
	void shouldloadSegmentExtension() {
		def segmentExtension = project.extensions.findByName("segment")
		assertNotNull(segmentExtension)
		assert segmentExtension instanceof GapWMSegmentDsl
		println segmentExtension
	}

	static def taskShouldExist(task, project) {
		assertThat("Task '${task}' does not exist on project", project.tasks.findByName(task), notNullValue())
	}
	
}
