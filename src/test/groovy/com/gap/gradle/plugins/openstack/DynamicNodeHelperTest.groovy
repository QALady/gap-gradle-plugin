package com.gap.gradle.plugins.openstack

import static org.mockito.Mockito.*

import org.junit.Before
import org.junit.Ignore
import org.mockito.Mockito

import com.gap.gradle.extensions.GapWMSegmentDslDynamicNodes
import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.ec.CommanderClient

class DynamicNodeHelperTest {
	private CommanderClient commanderClient
	private ShellCommand mockShellCommand
	private DynamicNodeHelper helper

	@Before
	void setup() {
		mockShellCommand = mock(ShellCommand, Mockito.RETURNS_SMART_NULLS)
		commanderClient = new CommanderClient(mockShellCommand)
		helper = new DynamicNodeHelper(commanderClient)
		helper.TIME_TO_WAIT_IN_MINUTES = 2
	}

	@Ignore
	void shouldRunWithDynamicNodes() {
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
	}
		

}
