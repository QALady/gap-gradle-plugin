package com.gap.gradle.plugins

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*
import static org.mockito.Mockito.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import com.gap.gradle.extensions.GapWMSegmentDsl
import com.gap.gradle.extensions.GapWMSegmentDslActionParameter

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
	}

	static def taskShouldExist(task, project) {
		assertThat("Task '${task}' does not exist on project", project.tasks.findByName(task), notNullValue())
	}

	@Test
	void shouldAssertSegmentExtensionLoaded() {
		project.segment {
			actions {
				smoke {
					action 'WM Exec:Run'
					parameters {
						cmd.value './gradlew tasks --info'
						abc.value '1234'
					}
				}
			}
		  }
		assertEquals("Something wrong", new GapWMSegmentDslActionParameter('abc', '1234').toString(), project.segment.actions.smoke.parameters.abc.toString())
	}

	@Test
	void shouldAssertSegmentExtensionLoadMultipleActions() {
		project.segment {
			actions {
				smoke {
					action 'WM Exec:Run'
					parameters {
						cmd.value './gradlew tasks --info'
					}
				}
				testAction {
					action 'echo "Hello"'
				}
				anotherTestAction {
					action 'echo "Again"'
				}
				noCommandAction {
					
				}
			}
		  }
		assertEquals("testAction segment.action is unable to load", 'echo "Hello"', project.segment.actions.testAction.action)
		assertEquals("anotherTestAction segment.action is unable to load", 'echo "Again"', project.segment.actions.anotherTestAction.action)
		assertNull("noCommandAction should not break the dsl load", project.segment.actions.noCommandAction.action)
		assertEquals(4, project.segment.actions.size())
	}
	
}
