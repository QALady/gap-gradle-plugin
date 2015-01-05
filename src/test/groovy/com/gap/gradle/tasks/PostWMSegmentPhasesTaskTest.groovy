package com.gap.gradle.tasks

import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.utils.EnvironmentStub
import org.apache.tools.ant.Project
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertEquals
import static org.mockito.Mockito.*

class PostWMSegmentPhasesTaskTest {

	private Project project
	private CommanderClient commanderClient
	private ShellCommand mockShellCommand
	PostWMSegmentPhasesTask task

	@Before
	void setUp() {
		project = ProjectBuilder.builder().build()
		project.apply plugin: 'gap-wm-segmentdsl'

		mockShellCommand= mock(ShellCommand, RETURNS_SMART_NULLS)
		EnvironmentStub env = new EnvironmentStub()
		env.setValue('COMMANDER_JOBID', '1234') // sets the job ID
		commanderClient= new CommanderClient(mockShellCommand,env)
		task = new PostWMSegmentPhasesTask(project, commanderClient)
	}

	@Test
	void shouldDeleteNodes() {
		println 'hello'
	}

	@Test
	void shouldCreateJobLinks() {
		println 'hello'
	}

	@Test
	void shouldAssertCreateECJobLinkIsFilledAndRunWithoutException() {
		project.segment {
			test {
				myAction {
					command 'ectool test'
				}
			}
			jobLinks {
				SonarPrepare {
					link "http://sonar001.phx.gapinc.dev:9000/dashboard/index/prepare"
				}
				SonarTest {
					link "http://sonar001.phx.gapinc.dev:9000/dashboard/index/test"
				}
				MyTestLink {
					link "http://dummytest.com/"
				}
				"MyTestLink With Space" {
					link "http://dummytest.com/"
				}

			}
		}
		task.execute()

		assertEquals("jobLinks unable to load", "SonarPrepare", project.segment.jobLinks.SonarPrepare.name)
		assertEquals("jobLinks unable to load", "http://sonar001.phx.gapinc.dev:9000/dashboard/index/prepare",
				project.segment.jobLinks.SonarPrepare.link)

		assertEquals("jobLinks unable to load", "SonarTest", project.segment.jobLinks.SonarTest.name)
		assertEquals("jobLinks unable to load", "http://sonar001.phx.gapinc.dev:9000/dashboard/index/test",
				project.segment.jobLinks.SonarTest.link)

		assertEquals(4, project.segment.jobLinks.size())

//        verify(mockShellCommand).execute(["ectool", "setProperty", "/myJob/report-urls/SonarPrepare", "http://sonar001.phx.gapinc.dev:9000/dashboard/index/prepare"])
//        verify(mockShellCommand).execute(["ectool", "setProperty", "/myJob/report-urls/SonarTest", "http://sonar001.phx.gapinc.dev:9000/dashboard/index/test"])
//        verify(mockShellCommand).execute(["ectool", "setProperty", "/myJob/report-urls/MyTestLink", "http://dummytest.com/"])
//        verify(mockShellCommand).execute(["ectool", "setProperty", "/myJob/report-urls/MyTestLink With Space", "http://dummytest.com/"])

		//verifyPhaseProcedures("prepare")
//        verifyPhaseProcedures("test")
//        verify(mockShellCommand).execute(["ectool", "getProperty", "/myProject/runCondition"])
//        verify(mockShellCommand).execute(["ectool", "createStep", "WM Temporary Procedures", "perform_test_actions_1234", "Perform myAction: ",
//                                          "--command", 'ectool test', "--condition", "always", "--parallel", "false"])

//        verifyPhaseProcedures("approve")
//        verifyPhaseProcedures("_finally")
	}

	private verifyPhaseProcedures(phase) {
		def phaseConfig = '_finally'.equals(phase) ? 'finally' : phase
		def procName = "perform_${phase}_actions_1234".toString()
		def configName = "/myJob/watchmen_config/${phaseConfig}StepProcedureName".toString()
//        verify(mockShellCommand).execute(["ectool", "createProcedure", "WM Temporary Procedures", procName, "--description", "dynamic procedure created by gradle task gap_wm_segmentdsl"])
//        verify(mockShellCommand).execute(["ectool", "setProperty", configName, procName])
	}
}
