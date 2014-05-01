package com.gap.gradle.tasks

import static helpers.CustomMatchers.sameString
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import com.gap.gradle.utils.ShellCommand


class UploadAthenaBuildRpmToRepoTest {
	private Task task
	private Project project

	@Before
	void setup() {
		project = ProjectBuilder.builder().build()
		project.athenaLocalRpmBase = "/tmp/local"
		def mockShellCommand = mock(ShellCommand)
		def task = new UploadAthenaBuildRpmToRepo(project, mockShellCommand)
	}
	
	@Test
	void testExecuteTask() {
		//task.execute()
		//verify(mockShellCommand).execute(sameString(
	}
}
