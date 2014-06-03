package com.gap.gradle.plugins

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class GapAthenaPluginTest {

	private Project project
	
	@Before
	void setup() {
		project = ProjectBuilder.builder().build()
		project.apply plugin: 'gapathena'
	}
	
   @Test
    void shouldExistTask_uploadBuildRpmToRepo() {
		taskShouldExist('uploadBuildRpmToRepo')
    }

	@Test
	void shouldExistTask_initNAPosBuild() {
		taskShouldExist('initNAPosBuild')
	}

    @Test
    void shouldExistTask_jarNAPosBuild() {
        taskShouldExist('jarNAPosBuild')
    }

	def taskShouldExist(task) {
		assertThat(project.tasks.findByName(task), notNullValue())
	}

}
