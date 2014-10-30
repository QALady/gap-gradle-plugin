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

	@Test
	void shouldAssertSegmentExtensionLoaded() {
		project.segment {
			actions {
				testCommand {
					command 'echo "Hello"'
				}
			}
		  }
		assertEquals("Something wrong", 'echo "Hello"', project.segment.actions.testCommand.command)
	}

	@Test
	void shouldAssertSegmentExtensionLoadMultipleActions() {
		project.segment {
			actions {
				testAction {
					command 'echo "Hello"'
				}
				anotherTestAction {
					command 'echo "Again"'
				}
				noCommandAction {
					
				}
			}
		  }
		assertEquals("testAction segment.action is unable to load", 'echo "Hello"', project.segment.actions.testAction.command)
		assertEquals("anotherTestAction segment.action is unable to load", 'echo "Again"', project.segment.actions.anotherTestAction.command)
		assertNull("noCommandAction should not break the dsl load", project.segment.actions.noCommandAction.command)
		assertEquals(3, project.segment.actions.size())
	}

	
}
