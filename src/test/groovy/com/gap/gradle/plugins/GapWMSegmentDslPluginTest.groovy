package com.gap.gradle.plugins

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import com.gap.gradle.extensions.GapWMSegmentDsl
import com.gap.gradle.extensions.GapWMSegmentDslActionParameter

class GapWMSegmentDslPluginTest {
	private Project project

	
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
	void shouldExistTaskPostWMSegmentPhases() {
		taskShouldExist('postWMSegmentPhases', project)
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
			prepare {
				smoke {
					resourceName='dgphxaciap003'
					action 'WM Exec:Run'
					parameters {
						cmd.value './gradlew tasks --info'
						abc.value '1234'
					}
				}
				testGradleInvoke {
					action 'WM Gradle:Invoke'
					parameters {
						tasks {
							value 'tasks --info'
						}
					}
				}
			}
		  }
		assertEquals("Something wrong", new GapWMSegmentDslActionParameter('abc', '1234').toString(), project.segment.prepare.smoke.parameters.abc.toString())
		assertEquals("unable to define tasks as parameter", new GapWMSegmentDslActionParameter('tasks', 'tasks --info').toString(), project.segment.prepare.testGradleInvoke.parameters.tasks.toString())
		assertEquals(0, project.segment.approve.size())
		assertEquals(0, project.segment._finally.size())
		assertEquals("ResourceName Wrong", 'dgphxaciap003', project.segment.prepare.smoke.resourceName)
	}

	@Test
	void shouldAssertSegmentExtensionLoadMultipleActions() {
		project.segment {
			prepare {
				smoke {
					action 'WM Exec:Run'
					parameters {
						cmd {
							value './gradlew tasks --info'
						}
					}
				}
				testAction {
					action 'echo "Hello"'
					parameters {
						param1 { //--actualParameter:'param1=test' --actualParameter:'param2=test2'
							value 'test'
						}
						param2 {
							value 'test2'
						}
					}
				}
				anotherTestAction {
					action 'echo "Again"'
				}
				noCommandAction {
					
				}
			}
			test {
				
			}
		  }
		assertEquals("testAction segment.action is unable to load", 'echo "Hello"', project.segment.prepare.testAction.action)
		assertEquals("anotherTestAction segment.action is unable to load", 'echo "Again"', project.segment.prepare.anotherTestAction.action)
		assertNull("noCommandAction should not break the dsl load", project.segment.prepare.noCommandAction.action)
		assertEquals(4, project.segment.prepare.size())
		assertEquals(0, project.segment.test.size())


	}

	@Test
	void shouldCreateDynamicNode()
	{
		project.segment {
			resourceName 'resourceTest01'
			prepare {
				smoke {
					action 'WM Exec:Run'
					parameters {
						cmd {
							value './gradlew tasks --info'
						}
					}
				}
				testAction { // this is the last because order is alphabetical
					action 'echo "Hello"'
					parameters {
						param1 { //--actualParameter:'param1=test' --actualParameter:'param2=test2'
							value 'test'
						}
						param2 {
							value 'test2'
						}
					}
				}
				anotherTestAction {
					action 'echo "Again"'
				}
				noCommandAction {

				}
			}
			test {

			}
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

		assertEquals('test',project.segment.prepare.testAction.parameters.param1.value)
		assertEquals("resourceTest01",project.segment.resourceName)

		assertEquals('tenant-name1', project.segment.dynamicNodes.node1.openstackTenant)
		assertEquals('the-chef-role-to-apply-on-the-node1', project.segment.dynamicNodes.node1.chefRole)


		assertEquals('tenant-name2', project.segment.dynamicNodes.node2.openstackTenant)
		assertEquals('the-chef-role-to-apply-on-the-node2', project.segment.dynamicNodes.node2.chefRole)
	}


}
