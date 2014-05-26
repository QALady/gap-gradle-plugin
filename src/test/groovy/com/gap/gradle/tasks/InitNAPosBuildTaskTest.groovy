package com.gap.gradle.tasks

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before

class InitNAPosBuildTaskTest {
	Project project
	private Task task

	@Before
	void setUp() {
		this.project = new ProjectBuilder().builder().build()
		project.apply plugin: 'gapathena'
		task = project.tasks.findByName('initNAPosBuild')
	}

}
