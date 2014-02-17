package com.gap.gradle.tasks

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before

class PrepareToPromoteToProductionTaskTest {
	private Task prepareToPromoteTask
	private Project project
	
	@Before
	setup() {
		project = ProjectBuilder.builder().build()
		project.apply plugin: 'gapproddeploy'
	}
}
