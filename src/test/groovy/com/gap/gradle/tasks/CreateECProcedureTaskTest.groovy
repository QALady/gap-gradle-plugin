package com.gap.gradle.tasks

import com.gap.gradle.exceptions.WMSegmentDslLockResourceOnLocalException
import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.utils.EnvironmentStub
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.mockito.Mockito

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNull
import static org.junit.rules.ExpectedException.none
import static org.mockito.Mockito.*

class CreateECProcedureTaskTest {
	private Project project
	private CommanderClient commanderClient
	private ShellCommand mockShellCommand
	static final String testGetPluginsXmlFileName = "src/test/groovy/com/gap/gradle/resources/testGetPlugins.xml"
	def logger = LogFactory.getLog(CreateECProcedureTaskTest)
	CreateECProcedureTask task

	@Rule
	public final ExpectedException expectedException = none()

	@Before
	void setup() {
		project = ProjectBuilder.builder().build()
		project.apply plugin: 'gap-wm-segmentdsl'

		mockShellCommand = mock(ShellCommand, Mockito.RETURNS_SMART_NULLS)
		when(mockShellCommand.execute(['ectool', 'getPlugin', 'WM Exec'])).thenReturn(new File(testGetPluginsXmlFileName).getText())
		when(mockShellCommand.execute(['ectool', 'createStep', 'WM Temporary Procedures', 'procedure1', 'Perform myAction: ', '--command', 'ectool test', '--resourceName', 'dgphxaciap003', '--condition', 'always', '--parallel', 'false'])).thenReturn("OK!")
		when(mockShellCommand.execute(['ectool', 'getProperty', '/myProject/runCondition'])).thenReturn('always')
		when(mockShellCommand.execute(['ectool', 'runProcedure', '"Watchmen Experimental"', '--procedureName', '"Create Dynamic Build Node"', '--actualParameter', 'openstackTenant=tenant-name1', 'chefRole=the-chef-role-to-apply-on-the-node1'])).thenReturn(1)
		when(mockShellCommand.execute(['ectool', 'runProcedure', '"Watchmen Experimental"', '--procedureName', '"Create Dynamic Build Node"', '--actualParameter', 'openstackTenant=tenant-name2', 'chefRole=the-chef-role-to-apply-on-the-node2'])).thenReturn(2)

		EnvironmentStub env = new EnvironmentStub()
		env.setValue('COMMANDER_JOBID', '1234') // sets the job ID
		commanderClient = new CommanderClient(mockShellCommand, env)
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

		def actualResult = task.createPhaseECStep("procedure1", project.segment.test.myAction)
		assertEquals("Bad formed command string", "OK!", actualResult)
	}

	@Test
	void shouldCreateProcedureSimpleCommandDsl() {
		project.segment {
			test {
				'test-simple-command' {
					command 'echo "Hello Test"'
				}
			}
		}
		task.execute()
		verifyPhaseProcedures("prepare")

		verifyPhaseProcedures("test")
		verify(mockShellCommand).execute(["ectool", "getProperty", "/myProject/runCondition"])
		verify(mockShellCommand).execute(["ectool", "createStep", "WM Temporary Procedures", "perform_test_actions_1234", "Perform test-simple-command: ",
										  "--command", 'echo "Hello Test"', "--condition", "always", "--parallel", "false"])

		verifyPhaseProcedures("approve")
		verifyPhaseProcedures("_finally")
	}

	@Test
	void shouldCreateProcedureSimplePluginDsl() {
		project.segment {
			test {
				'test-simple-plugin' {
					action 'WM Exec:Run'
					parameters {
						cmd {
							value './gradlew tasks --info'
						}
					}
				}
			}
		}
		task.execute()
		verifyPhaseProcedures("prepare")

		verifyPhaseProcedures("test")
		verify(mockShellCommand).execute(["ectool", "getPlugin", "WM Exec"])
		verify(mockShellCommand).execute(["ectool", "getProperty", "/myProject/runCondition"])
		verify(mockShellCommand).execute(["ectool", "createStep", "WM Temporary Procedures", "perform_test_actions_1234", "Perform test-simple-plugin: WM Exec:Run",
										  "--subproject", "WM Exec-1.17", "--subprocedure", "Run", "--actualParameter", "cmd=./gradlew tasks --info", "--condition", "always", "--parallel", "false"])

		verifyPhaseProcedures("approve")
		verifyPhaseProcedures("_finally")
	}

	@Test
	void shouldCreateProcedureWithLockResourceDefined() {
		project.segment {
			test {
				'test-local-resource-defined' {
					action 'WM Segment:Lock Resource'
					parameters {
						host.value 'my-dummy-resource-host'
					}
				}
			}
		}
		task.execute()
		verifyPhaseProcedures("prepare")

		verifyPhaseProcedures("test")
		verify(mockShellCommand).execute(["ectool", "getPlugin", "WM Segment"])
		verify(mockShellCommand).execute(["ectool", "getProperty", "/myProject/runCondition"])
		verify(mockShellCommand).execute(["ectool", "createStep", "WM Temporary Procedures", "perform_test_actions_1234", "Perform test-local-resource-defined: WM Segment:Lock Resource",
										  "--subproject", "WM Segment", "--subprocedure", "Lock Resource", "--actualParameter", "host=my-dummy-resource-host", "--condition", "always", "--parallel", "false"])

		verifyPhaseProcedures("approve")
		verifyPhaseProcedures("_finally")
	}

	@Test
	void shouldCreateProcedureWithLockResourceEmpty() {
		expectedException.expect(WMSegmentDslLockResourceOnLocalException)
		project.segment {
			test {
				'test-local-resource-defined' {
					action 'WM Segment:Lock Resource'
				}
			}
		}
		task.execute()
		verifyPhaseProcedures("prepare")

		verifyPhaseProcedures("test")
		verify(mockShellCommand).execute(["ectool", "getPlugin", "WM Segment"])
		verify(mockShellCommand).execute(["ectool", "getProperty", "/myProject/runCondition"])

		verifyPhaseProcedures("approve")
		verifyPhaseProcedures("_finally")
	}

	@Test
	void shouldCreateProcedureWithLockResourceHostLocal() {
		expectedException.expect(WMSegmentDslLockResourceOnLocalException)
		project.segment {
			test {
				'test-local-resource-defined' {
					action 'WM Segment:Lock Resource'
					parameters {
						host.value 'local'
					}
				}
			}
		}
		task.execute()
		verifyPhaseProcedures("prepare")

		verifyPhaseProcedures("test")
		verify(mockShellCommand).execute(["ectool", "getPlugin", "WM Segment"])
		verify(mockShellCommand).execute(["ectool", "getProperty", "/myProject/runCondition"])

		verifyPhaseProcedures("approve")
		verifyPhaseProcedures("_finally")
	}

	@Test
	void shouldCreateProcedureWithLockResourceHostEmpty() {
		expectedException.expect(WMSegmentDslLockResourceOnLocalException)
		project.segment {
			test {
				'test-local-resource-defined' {
					action 'WM Segment:Lock Resource'
					parameters {
						host.value ''
					}
				}
			}
		}
		task.execute()
		verifyPhaseProcedures("prepare")

		verifyPhaseProcedures("test")
		verify(mockShellCommand).execute(["ectool", "getPlugin", "WM Segment"])
		verify(mockShellCommand).execute(["ectool", "getProperty", "/myProject/runCondition"])

		verifyPhaseProcedures("approve")
		verifyPhaseProcedures("_finally")
	}

	@Test
	void shouldCreateProcedureWithLockResourceHostBlank() {
		expectedException.expect(WMSegmentDslLockResourceOnLocalException)
		project.segment {
			test {
				'test-local-resource-defined' {
					action 'WM Segment:Lock Resource'
					parameters {
						host.value '  '
					}
				}
			}
		}
		task.execute()
		verifyPhaseProcedures("prepare")

		verifyPhaseProcedures("test")
		verify(mockShellCommand).execute(["ectool", "getPlugin", "WM Segment"])
		verify(mockShellCommand).execute(["ectool", "getProperty", "/myProject/runCondition"])

		verifyPhaseProcedures("approve")
		verifyPhaseProcedures("_finally")
	}

	private verifyPhaseProcedures(phase) {
		def phaseConfig = '_finally'.equals(phase) ? 'finally' : phase
		def procName = "perform_${phase}_actions_1234".toString()
		def configName = "/myJob/watchmen_config/${phaseConfig}StepProcedureName".toString()
		verify(mockShellCommand).execute(["ectool", "createProcedure", "WM Temporary Procedures", procName, "--description", "dynamic procedure created by gradle task gap_wm_segmentdsl"])
		verify(mockShellCommand).execute(["ectool", "setProperty", configName, procName])
	}

	@Ignore
	void shouldRunWithDynamicNodes() {

		project.segment {
			dynamicNodes {
				node1 {
					openstackTenant 'tenant-name1'
					chefRole 'the-chef-role-to-apply-on-the-node1'
				}
				node2 {
					openstackTenant 'tenant-name2'
					chefRole 'the-chef-role-to-apply-on-the-node2'
				}
			}
		}
		task.execute()
	}

}
