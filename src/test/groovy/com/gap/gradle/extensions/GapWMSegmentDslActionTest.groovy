package com.gap.gradle.extensions

import static org.junit.Assert.*
import static org.mockito.Mockito.*

import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator
import org.junit.Before
import org.junit.Test


class GapWMSegmentDslActionTest {
	GapWMSegmentDslAction segmentAction
	
	@Before
	void setup() {
		segmentAction = new GapWMSegmentDslAction('test0', mock(Project), mock(Instantiator))
	}
	
	@Test
	void shouldCheckIfActionHasSubProject() {
		segmentAction.action = "Test EC Project:Test EC procedure"
		assertTrue("given action has no subProject, does not have :", segmentAction.hasSubProject())
	}
	
	@Test
	void shouldCheckIfActionHasNoSubProject() {
		segmentAction.action = "Test EC ProjectTest EC procedure"
		assertFalse("given action has no subProject, does not have :", segmentAction.hasSubProject())
	}
	
}
