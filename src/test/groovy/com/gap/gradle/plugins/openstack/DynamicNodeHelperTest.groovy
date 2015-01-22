package com.gap.gradle.plugins.openstack

import com.gap.gradle.exceptions.DynamicNodesException
import com.gap.gradle.extensions.GapWMSegmentDslDynamicNodes
import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.ec.CommanderClient
import helpers.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

class DynamicNodeHelperTest {
	private CommanderClient commanderClient
	private ShellCommand mockShellCommand
	private DynamicNodeHelper helper

	@Before
	void setup() {
		mockShellCommand = mock(ShellCommand, Mockito.RETURNS_SMART_NULLS)
		commanderClient = new CommanderClient(mockShellCommand)
		helper = new DynamicNodeHelper(commanderClient)
		helper.TIME_TO_WAIT_IN_MINUTES = 0.1
		helper.INTERVAL_IN_MINUTES = 0.1
	}

	@Test
	void shouldRunWithDynamicNodesWithNoException() {

		when(mockShellCommand.execute(['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Create', '--actualParameter', 'tenant=tenant-name1', 'roleName=the-chef-role-to-apply-on-the-node1', 'hostname=node1', 'network=public', 'autoPurge=true', 'createResource=true', 'type=null'])).thenReturn('1')
		when(mockShellCommand.execute(['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Create', '--actualParameter', 'tenant=tenant-name2', 'roleName=the-chef-role-to-apply-on-the-node2', 'hostname=node2', 'network=public', 'autoPurge=true', 'createResource=true', 'type=null'])).thenReturn('2')
		when(mockShellCommand.execute(['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Create', '--actualParameter', 'tenant=tenant-name3', 'roleName=the-chef-role-to-apply-on-the-node3', 'hostname=node3', 'network=public', 'autoPurge=true', 'createResource=true', 'type=null'])).thenReturn('3')
		def jobStatusResponse1 = """<response requestId="1" nodeId="10.105.68.77">
    <jobId>1</jobId>
    <outcome>success</outcome>
    <status>completed</status>
  </response>"""

		def jobStatusResponse2 = """<response requestId="1" nodeId="10.105.68.77">
    <jobId>2</jobId>
    <outcome>success</outcome>
    <status>completed</status>
  </response>"""

		def jobStatusResponse3 = """<response requestId="1" nodeId="10.105.68.77">
    <jobId>3</jobId>
    <outcome>success</outcome>
    <status>completed</status>
  </response>"""

		when(mockShellCommand.execute('ectool getJobStatus 1')).thenReturn(jobStatusResponse1.toString())
		when(mockShellCommand.execute('ectool getJobStatus 2')).thenReturn(jobStatusResponse2.toString())
		when(mockShellCommand.execute('ectool getJobStatus 3')).thenReturn(jobStatusResponse3.toString())

		GapWMSegmentDslDynamicNodes node1 = new GapWMSegmentDslDynamicNodes("node1")
		node1.openstackTenant = 'tenant-name1'
		node1.chefRole = 'the-chef-role-to-apply-on-the-node1'

		GapWMSegmentDslDynamicNodes node2 = new GapWMSegmentDslDynamicNodes("node2")
		node2.openstackTenant = 'tenant-name2'
		node2.chefRole = 'the-chef-role-to-apply-on-the-node2'

		GapWMSegmentDslDynamicNodes node3 = new GapWMSegmentDslDynamicNodes("node3")
		node3.openstackTenant = 'tenant-name3'
		node3.chefRole = 'the-chef-role-to-apply-on-the-node3'

		def nodes = [node1, node2, node3]

		helper.createDynamicNodes(nodes)

		def command1= ['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Create', '--actualParameter', 'tenant=tenant-name1', 'roleName=the-chef-role-to-apply-on-the-node1', 'hostname=node1', 'network=public', 'autoPurge=true', 'createResource=true', 'type=null']
		def command2= ['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Create', '--actualParameter', 'tenant=tenant-name2', 'roleName=the-chef-role-to-apply-on-the-node2', 'hostname=node2', 'network=public', 'autoPurge=true', 'createResource=true', 'type=null']
		def command3= ['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Create', '--actualParameter', 'tenant=tenant-name3', 'roleName=the-chef-role-to-apply-on-the-node3', 'hostname=node3', 'network=public', 'autoPurge=true', 'createResource=true', 'type=null']

		verify(mockShellCommand).execute(command1)
		verify(mockShellCommand).execute(command2)
		verify(mockShellCommand).execute(command3)

		verify(mockShellCommand, Mockito.times(4)).execute('ectool getJobStatus 1')
		verify(mockShellCommand, Mockito.times(4)).execute('ectool getJobStatus 2')
		verify(mockShellCommand, Mockito.times(4)).execute('ectool getJobStatus 3')

	}

	@Test(expected = DynamicNodesException.class)
	void shouldRunWithDynamicNodesException() {

		when(mockShellCommand.execute(['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Create', '--actualParameter', 'tenant=tenant-name1', 'roleName=the-chef-role-to-apply-on-the-node1', 'hostname=node1', 'network=public', 'autoPurge=true', 'createResource=true', 'type=null'])).thenReturn('1')
		when(mockShellCommand.execute(['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Create', '--actualParameter', 'tenant=tenant-name2', 'roleName=the-chef-role-to-apply-on-the-node2', 'hostname=node2', 'network=public', 'autoPurge=true', 'createResource=true', 'type=null'])).thenReturn('2')
		when(mockShellCommand.execute(['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Create', '--actualParameter', 'tenant=tenant-name3', 'roleName=the-chef-role-to-apply-on-the-node3', 'hostname=node3', 'network=public', 'autoPurge=true', 'createResource=true', 'type=null'])).thenReturn('3')
		def jobStatusResponse1 = """<response requestId="1" nodeId="10.105.68.77">
    <jobId>1</jobId>
    <outcome>success</outcome>
    <status>completed</status>
  </response>"""

		def jobStatusResponse2 = """<response requestId="1" nodeId="10.105.68.77">
    <jobId>2</jobId>
    <outcome>error</outcome>
    <status>completed</status>
  </response>"""

		def jobStatusResponse3 = """<response requestId="1" nodeId="10.105.68.77">
    <jobId>3</jobId>
    <outcome>success</outcome>
    <status>completed</status>
  </response>"""

		when(mockShellCommand.execute('ectool getJobStatus 1')).thenReturn(jobStatusResponse1.toString())
		when(mockShellCommand.execute('ectool getJobStatus 2')).thenReturn(jobStatusResponse2.toString())
		when(mockShellCommand.execute('ectool getJobStatus 3')).thenReturn(jobStatusResponse3.toString())

		GapWMSegmentDslDynamicNodes node1 = new GapWMSegmentDslDynamicNodes("node1")
		node1.openstackTenant = 'tenant-name1'
		node1.chefRole = 'the-chef-role-to-apply-on-the-node1'

		GapWMSegmentDslDynamicNodes node2 = new GapWMSegmentDslDynamicNodes("node2")
		node2.openstackTenant = 'tenant-name2'
		node2.chefRole = 'the-chef-role-to-apply-on-the-node2'

		GapWMSegmentDslDynamicNodes node3 = new GapWMSegmentDslDynamicNodes("node3")
		node3.openstackTenant = 'tenant-name3'
		node3.chefRole = 'the-chef-role-to-apply-on-the-node3'

		def nodes = [node1, node2, node3]
		helper.createDynamicNodes(nodes)

		def commandCreate1= ['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Create', '--actualParameter', 'tenant=tenant-name1', 'roleName=the-chef-role-to-apply-on-the-node1', 'hostname=node1', 'network=public', 'autoPurge=true', 'createResource=true', 'type=null']
		def commandCreate2= ['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Create', '--actualParameter', 'tenant=tenant-name2', 'roleName=the-chef-role-to-apply-on-the-node2', 'hostname=node2', 'network=public', 'autoPurge=true', 'createResource=true', 'type=null']
		def commandCreate3= ['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Create', '--actualParameter', 'tenant=tenant-name3', 'roleName=the-chef-role-to-apply-on-the-node3', 'hostname=node3', 'network=public', 'autoPurge=true', 'createResource=true', 'type=null']

		verify(mockShellCommand).execute(commandCreate1)
		verify(mockShellCommand).execute(commandCreate2)
		verify(mockShellCommand).execute(commandCreate3)

		verify(mockShellCommand, Mockito.times(4)).execute('ectool getJobStatus 1')
		verify(mockShellCommand, Mockito.times(4)).execute('ectool getJobStatus 2')
		verify(mockShellCommand, Mockito.times(4)).execute('ectool getJobStatus 3')

		verify(mockShellCommand).execute(commandCreate1)
		verify(mockShellCommand).execute(commandCreate2)
		verify(mockShellCommand).execute(commandCreate3)

		def commandDelete1=['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Delete', '--actualParameter', 'resourceToDelete=node1', 'tenant=tenant-name1']
		def commandDelete2=['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Delete', '--actualParameter', 'resourceToDelete=node2', 'tenant=tenant-name2']
		def commandDelete3=['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Delete', '--actualParameter', 'resourceToDelete=node3', 'tenant=tenant-name3']

		verify(mockShellCommand).execute(commandDelete1)
		verify(mockShellCommand).execute(commandDelete2)
		verify(mockShellCommand).execute(commandDelete3)
	}


	@Test(expected = DynamicNodesException.class)
	void shouldRunWithRetryTimeOutException() {

		when(mockShellCommand.execute(['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Create', '--actualParameter', 'tenant=tenant-name1', 'roleName=the-chef-role-to-apply-on-the-node1', 'hostname=node1', 'network=public', 'autoPurge=true', 'createResource=true', 'type=null'])).thenReturn('1')
		when(mockShellCommand.execute(['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Create', '--actualParameter', 'tenant=tenant-name2', 'roleName=the-chef-role-to-apply-on-the-node2', 'hostname=node2', 'network=public', 'autoPurge=true', 'createResource=true', 'type=null'])).thenReturn('2')
		when(mockShellCommand.execute(['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Create', '--actualParameter', 'tenant=tenant-name3', 'roleName=the-chef-role-to-apply-on-the-node3', 'hostname=node3', 'network=public', 'autoPurge=true', 'createResource=true', 'type=null'])).thenReturn('3')
		def jobStatusResponse1 = """<response requestId="1" nodeId="10.105.68.77">
    <jobId>1</jobId>
    <outcome>success</outcome>
    <status>completed</status>
  </response>"""

		def jobStatusResponse2 = """<response requestId="1" nodeId="10.105.68.77">
    <jobId>2</jobId>
    <outcome>success</outcome>
    <status>running</status>
  </response>"""

		def jobStatusResponse3 = """<response requestId="1" nodeId="10.105.68.77">
    <jobId>3</jobId>
    <outcome>success</outcome>
    <status>completed</status>
  </response>"""

		when(mockShellCommand.execute('ectool getJobStatus 1')).thenReturn(jobStatusResponse1.toString())
		when(mockShellCommand.execute('ectool getJobStatus 2')).thenReturn(jobStatusResponse2.toString())
		when(mockShellCommand.execute('ectool getJobStatus 3')).thenReturn(jobStatusResponse3.toString())

		GapWMSegmentDslDynamicNodes node1 = new GapWMSegmentDslDynamicNodes("node1")
		node1.openstackTenant = 'tenant-name1'
		node1.chefRole = 'the-chef-role-to-apply-on-the-node1'

		GapWMSegmentDslDynamicNodes node2 = new GapWMSegmentDslDynamicNodes("node2")
		node2.openstackTenant = 'tenant-name2'
		node2.chefRole = 'the-chef-role-to-apply-on-the-node2'

		GapWMSegmentDslDynamicNodes node3 = new GapWMSegmentDslDynamicNodes("node3")
		node3.openstackTenant = 'tenant-name3'
		node3.chefRole = 'the-chef-role-to-apply-on-the-node3'

		def nodes = [node1, node2, node3]
		helper.createDynamicNodes(nodes)

		def commandCreate1= ['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Create', '--actualParameter', 'tenant=tenant-name1', 'roleName=the-chef-role-to-apply-on-the-node1', 'hostname=node1', 'network=public', 'autoPurge=true', 'createResource=true', 'type=null']
		def commandCreate2= ['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Create', '--actualParameter', 'tenant=tenant-name2', 'roleName=the-chef-role-to-apply-on-the-node2', 'hostname=node2', 'network=public', 'autoPurge=true', 'createResource=true', 'type=null']
		def commandCreate3= ['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Create', '--actualParameter', 'tenant=tenant-name3', 'roleName=the-chef-role-to-apply-on-the-node3', 'hostname=node3', 'network=public', 'autoPurge=true', 'createResource=true', 'type=null']

		verify(mockShellCommand).execute(commandCreate1)
		verify(mockShellCommand).execute(commandCreate2)
		verify(mockShellCommand).execute(commandCreate3)

		verify(mockShellCommand, Mockito.times(4)).execute('ectool getJobStatus 1')
		verify(mockShellCommand, Mockito.times(4)).execute('ectool getJobStatus 2')
		verify(mockShellCommand, Mockito.times(4)).execute('ectool getJobStatus 3')

		verify(mockShellCommand).execute(commandCreate1)
		verify(mockShellCommand).execute(commandCreate2)
		verify(mockShellCommand).execute(commandCreate3)

		def commandDelete1=['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Delete', '--actualParameter', 'resourceToDelete=node1', 'tenant=tenant-name1']
		def commandDelete2=['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Delete', '--actualParameter', 'resourceToDelete=node2', 'tenant=tenant-name2']
		def commandDelete3=['ectool', 'runProcedure', 'Nova-CLI', '--procedureName', 'Easy Delete', '--actualParameter', 'resourceToDelete=node3', 'tenant=tenant-name3']

		verify(mockShellCommand).execute(commandDelete1)
		verify(mockShellCommand).execute(commandDelete2)
		verify(mockShellCommand).execute(commandDelete3)
	}


}
