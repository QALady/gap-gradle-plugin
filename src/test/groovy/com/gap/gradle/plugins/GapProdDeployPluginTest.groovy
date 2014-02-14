package com.gap.gradle.plugins

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import com.gap.gradle.plugins.cookbook.ConfigFileResource
import com.gap.gradle.plugins.cookbook.JenkinsConfig
import com.gap.gradle.tasks.DeployToProductionTask

class GapProdDeployPluginTest {

	@Rule
	public final ConfigFileResource config = new ConfigFileResource(GapProdDeployPlugin, "CONFIG_FILE")

	private Project project
	private static String pluginName = 'gapproddeploy'
	private static String testJson = "src/test/groovy/com/gap/gradle/resources/testProdDeployParams.json"
	
	@Before
	void setup() {
		project = ProjectBuilder.builder().build()
		project.paramJsonPath = testJson
		project.apply plugin: pluginName
	}
	
	@Test
	void deployToProdDeployTaskIsAddedToProject(){
		taskShouldExist('deployToProduction')
	}

	@Test
	void promoteToProdDeployTaskIsAddedToProject(){
		taskShouldExist('promoteToProduction')
	}

	@Test
	void prepareToPromoteTaskIsAddedToProject() {
		taskShouldExist('prepareToPromote')
	}

	@Test
	void testJenkinsProjectExtensionIsLoaded() {
		def jenkinsConfig = project.extensions.findByName("jenkins")
		assertNotNull(jenkinsConfig)
	}

	@Test
	void testProdDeployProjectExtensionIsLoaded() {
		def prodDeploy = project.extensions.findByName("prodDeploy")
		assertNotNull(prodDeploy)
	}

	@Test
	void testJenkinsExtensionConfigurationDoesNotLoadAgain() {
		project = ProjectBuilder.builder().build()
		project.paramJsonPath = testJson
		project.extensions.create("jenkins", JenkinsConfig) // already defining the jenkins extension
		project.apply plugin: pluginName // this should not complain that jenkins already exist on the project
		def jenkinsConfig = project.extensions.findByName("jenkins")
		assertNotNull(jenkinsConfig)
	}

	@Test	
	void shouldReadJsonFromConfigFile() {
		def expectedsha1s = ["38615ae7ac61737184440a5797fa7becd4f684c8", "28615ae7ac61737184440a5797fa7becd4f684c8"]
        assertThat(project.prodDeploy.appVersion, equalTo("976"))
		assertThat(project.prodDeploy.sha1IdList, equalTo(expectedsha1s))
		assertThat(project.prodDeploy.roleName, equalTo("test-role-name"))
		assertThat(project.prodDeploy.cookbook.name, equalTo("test-app"))
		assertThat(project.prodDeploy.cookbook.sha1Id, equalTo("38615ae7ac61737184440a5797fa7becd4f684c7"))
		assertThat(project.prodDeploy.nodes, equalTo("[testnode01.phx.gapinc.dev,testnode02.phx.gapinc.dev]"))
	}

	def taskShouldExist(task) {
		assertThat(project.tasks.findByName(task), notNullValue())
	}
}
