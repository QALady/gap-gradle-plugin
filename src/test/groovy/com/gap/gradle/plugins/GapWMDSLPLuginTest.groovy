package com.gap.gradle.plugins

import com.gap.gradle.extensions.WatchmenDSLExtension
import com.gap.gradle.tasks.CreateECProcedureTask
import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.utils.EnvironmentStub
import groovy.json.JsonSlurper
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito

import static org.hamcrest.Matchers.notNullValue
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class GapWMDSLPLuginTest {

	def logger = LogFactory.getLog(GapWMDSLPLuginTest)

	private Project project

	WatchmenDSLExtension extension

	private CommanderClient commanderClient

	private ShellCommand mockShellCommand

	EnvironmentStub environmentStub

	//final String testGetPluginsXmlFileName = "src/test/groovy/com/gap/gradle/resources/testGetPlugins.xml"
	final String testGetPluginsJsonFileName = "src/test/groovy/com/gap/gradle/resources/testGetPlugins.json"

	CreateECProcedureTask task

	def watchmenConfigSheet = "/myJob/watchmen_config"

	@Before
	void setup() {

		project = ProjectBuilder.builder().build()

		project.apply plugin: 'gap-wm-dsl'

		environmentStub = new EnvironmentStub()

		mockShellCommand = mock(ShellCommand, Mockito.RETURNS_SMART_NULLS)

		when(mockShellCommand.execute(['ectool', '--format', 'json', 'getPlugins'])).thenReturn(new File(testGetPluginsJsonFileName).getText())

		commanderClient = new CommanderClient(mockShellCommand, environmentStub)


		task = new CreateECProcedureTask(project, commanderClient)

/** **************************************************************/

//		my $jobId = '$[jobId]';
//		my $projectName = "WM Temporary Procedures";
//		my $watchmenConfigSheet = '/myJob/watchmen_config';
//		my $version = '/myJob/version';

		project.jobId = "1234"
		def projectName = "WM Temporary Procedures"

		def version = "/myJob/version"

		//	my @phaseList = ('prepare','test','approve','finally');


	}

	/*#create procedures for valid types
	foreach my $phase (@phaseList) {
		createPhaseProcedure($phase, fetchPhaseConfiguration($phase) );
		#TODO create the "do $phase" procs to call the temp procedures?
	}
*/

	@Ignore
	void shouldCreatePhaseProcedureForPhaseList() {
		task.phaseList.each { phase ->

			def actionHash = fetchPhaseConfiguration(phase)
			createPhaseProcedure(phase, actionHash)
		}
	}

	@Ignore
	void fetchPhaseConfigurationTest() {
		def leftovers = []
		def actionHash = [:]

		task.phaseList.each { phase ->
			def phaseElements = commanderClient.getECProperties([path: "${watchmenConfigSheet+"/"+phase}", recurse: 1, expand: 0]);
			print("phaseElements : $phaseElements")
		}


	}


	@Test
	void shouldGetPlugins() {
		def expectedFile = new File(testGetPluginsJsonFileName)
		def expectedPluginsData = new JsonSlurper().parseText(expectedFile.getText())
		def actualPluginsData = task.getPlugins()

		assertEquals(expectedPluginsData.plugin.findAll { it.promoted == '1' }.size(), actualPluginsData.size())
	}

	@Test
	void shouldExistTasks() {
		taskShouldExist('createECProcedure', project)
	}

	static def taskShouldExist(task, project) {
		assertThat("Task '${task}' does not exist on project", project.tasks.findByName(task), notNullValue())
	}
}
