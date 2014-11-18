package com.gap.gradle.tasks

import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.utils.EnvironmentStub
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class CheckDSLFileExistTaskTest {
	def logger = LogFactory.getLog(CheckDSLFileExistTaskTest)

	private Project project
	private CommanderClient commanderClient
	private ShellCommand mockShellCommand
	CheckDSLFileExistTask task

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder()

	@Before
	void setup() {
		project = ProjectBuilder.builder().withProjectDir(new File(temporaryFolder.root.path)).build();
		project.apply plugin: 'gap-wm-segmentdsl'
		mockShellCommand = mock(ShellCommand, Mockito.RETURNS_SMART_NULLS)

		initMock()

		commanderClient = new CommanderClient(mockShellCommand, new EnvironmentStub())
		task = new CheckDSLFileExistTask(project, commanderClient)
	}

	def initMock() {
		when(mockShellCommand.execute(['ectool', 'getProperty', '/myJob/watchmen_config/workingDir'])).thenReturn('ci')
		when(mockShellCommand.execute(['ectool', 'getProperty', '/myJob/watchmen_config/svn'])).thenReturn('false')
		when(mockShellCommand.execute(['ectool', 'getProperty', '/myJob/watchmen_config/git'])).thenReturn('true')
		when(mockShellCommand.execute(['ectool', 'getProperty', '/myJob/watchmen_config/segmentName'])).thenReturn('Segment Name')
	}

	@Test
	void shouldInitializeProperties() {
		def expectedSegmentName = 'Segment Name'
//		def expectedIsSvn=false
//		def expectedIsGit=true
		def expectedWorkingDir = 'ci'

		task.initializeProperties()

		assertEquals('Property Value differs', expectedSegmentName, task.segmentName)
//		assertEquals('Property Value differs', expectedIsSvn, task.isSvn)
//		assertEquals('Property Value differs', expectedIsGit, task.isGit)
		assertEquals('Property Value differs', expectedWorkingDir, task.workingDir)
	}


	@Test
	void shouldIdentifySegmentType() {
		CheckDSLFileExistTask.SegmentType appSegmentType = CheckDSLFileExistTask.SegmentType.app_segment
		def componentSegmentType = CheckDSLFileExistTask.SegmentType.component_segment
		def normalSegmentType = CheckDSLFileExistTask.SegmentType.normal_segment

		task.segmentName = 'app-segment'
		def actualSegmentType1 = task.identifySegmentType()
		assertEquals('Segment incorrect', appSegmentType, actualSegmentType1)
		logger.info "Segment type for ${task.segmentName} is $actualSegmentType1"

		task.segmentName = 'component-segment'
		def actualSegmentType2 = task.identifySegmentType()
		assertEquals('Segment incorrect', componentSegmentType, actualSegmentType2)
		logger.info "Segment type for ${task.segmentName} is $actualSegmentType2"

		task.segmentName = 'other-segment-type'
		def actualSegmentType3 = task.identifySegmentType()
		assertEquals('Segment incorrect', normalSegmentType, actualSegmentType3)
		logger.info "Segment type for ${task.segmentName} is $actualSegmentType3"
	}

	@Test
	void shouldCheckIfPropertiesFileExists() {
		task.segmentName = 'other-segment-type'

		File projectDirCi = new File(project.getProjectDir().toString() /*+ "/ci"*/)
		projectDirCi.mkdirs()

		String propertiesFileName = task.segmentName + ".properties"
		File propertiesFile = new File(projectDirCi, propertiesFileName)
		propertiesFile.createNewFile()

		logger.info("Expected Properties File : ${propertiesFile.getAbsoluteFile()}")

		def fileExists = task.checkIfPropertiesFileExists()
		logger.info("File exists = $fileExists")
		assertTrue('Error finding file', fileExists)
	}

}
