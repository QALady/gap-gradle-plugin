package com.gap.gradle.tasks

import static org.junit.Assert.*
import static org.mockito.Mockito.*
import groovy.json.JsonSlurper

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
		when(mockShellCommand.execute(['ectool', 'getPlugins'])).thenReturn(new File(testGetPluginsXmlFileName).getText())
		commanderClient = new CommanderClient(mockShellCommand, new EnvironmentStub())
		task = new CreateECProcedureTask(project, commanderClient)
	}

	@Test
	void shouldGetPlugins() {
		def expectedPluginsData = new XmlSlurper().parseText(new File(testGetPluginsXmlFileName).getText())
		def actualPluginsData = task.getPromotedPlugins()

		assertEquals(expectedPluginsData.plugin.findAll { it.promoted == '1' }.size(), actualPluginsData.size())
	}

	@Test
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

		assertEquals(expectedPluginsData.plugin.findAll { it.promoted == '1' }.size(), task.plugins.size())
	}

}
