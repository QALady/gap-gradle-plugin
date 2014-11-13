package com.gap.gradle.tasks

import static org.junit.Assert.*
import static org.mockito.Mockito.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito

import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.utils.EnvironmentStub

class CreateECProcedureTaskTest {
	private Project project
	private CommanderClient commanderClient
	private ShellCommand mockShellCommand
	static final String testGetPluginsXmlFileName = "src/test/groovy/com/gap/gradle/resources/testGetPlugins.xml"

	CreateECProcedureTask task

	@Before
	void setup() {
		project = ProjectBuilder.builder().build()
		project.apply plugin: 'gap-wm-segmentdsl'

		mockShellCommand = mock(ShellCommand, Mockito.RETURNS_SMART_NULLS)
		when(mockShellCommand.execute(['ectool', 'getPlugin', 'WM Exec'])).thenReturn(new File(testGetPluginsXmlFileName).getText())
		when(mockShellCommand.execute(['ectool', 'createStep', 'WM Temporary Procedures', 'procedure1', 'Perform myAction: ', '--command', 'ectool test', '--resourceName', 'dgphxaciap003', '--condition', '$[/myProject/runCondition]', '--parallel', 'false'])).thenReturn("OK!")
		commanderClient = new CommanderClient(mockShellCommand, new EnvironmentStub())
		task = new CreateECProcedureTask(project, commanderClient)
	}

	@Test
	void shouldGetPromotedPluginDataWhenAvailable() {
		def expectedPluginsData = 'WM Exec-1.17' // set in testdata testGetPlugins.xml
		def actualPluginsData = task.checkPromotedPlugin('WM Exec')
		assertEquals(expectedPluginsData, actualPluginsData.toString())
	}

	@Test
	void shouldGetSameGivenNameWhenNoPluginDataAvailable() {
		def expectedPluginsData = 'TestPluginWhichDoesNotExist'
		def actualPluginsData = task.checkPromotedPlugin(expectedPluginsData)
		assertEquals(expectedPluginsData, actualPluginsData)
	}

	@Ignore
	void shouldRunECProcedureActions() {
		def expectedPluginsData = new XmlSlurper().parseText(new File(testGetPluginsXmlFileName).getText())
		project.segment {
			prepare {
				smoke {
					action 'WM Exec:Run'
					parameters {
						cmd.value './gradlew tasks --info'
					}
				}
			}
		  }

		task.execute()
	}

	@Test
	void shouldAssertResourNameIsFilled() {
		project.segment {
			prepare {
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
			test {
				myAction
				{
					resourceName 'dgphxaciap003'
					command 'ectool test'
				}

			}
		}

		assertEquals("testAction segment.action is unable to load", 'echo "Hello"', project.segment.prepare.testAction.action)
		assertEquals("anotherTestAction segment.action is unable to load", 'echo "Again"', project.segment.prepare.anotherTestAction.action)
		assertNull("noCommandAction should not break the dsl load", project.segment.prepare.noCommandAction.action)
		assertEquals(4, project.segment.prepare.size())
		assertEquals("Problem with command", 'ectool test', project.segment.test.myAction.command)
		assertEquals("Problem with resourceName", 'dgphxaciap003', project.segment.test.myAction.resourceName)

		def actualResult=task.createPhaseECStep("procedure1", project.segment.test.myAction)
		assertEquals("Bad formed command string","OK!",actualResult)
	}

}
