package com.gap.gradle.plugins

import com.gap.gradle.tasks.PromoteArtifactsToProdTask

import static helpers.Assert.shouldExecuteTask
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
import groovy.mock.interceptor.MockFor
import com.gap.gradle.tasks.PromoteRpmTask
import com.gap.gradle.tasks.PrepareToPromoteToProductionTask

import static helpers.CustomMatchers.sameString

class GapProdDeployPluginTest {

	@Rule
	public final ConfigFileResource config = new ConfigFileResource(GapProdDeployPlugin, "CONFIG_FILE")

	private Project project
	private static String pluginName = 'gapproddeploy'
	private static String testJsonPath = "src/test/groovy/com/gap/gradle/resources/"
	
	@Before
	void setup() {
		project = ProjectBuilder.builder().build()
		project.paramJsonPath = testJsonPath
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
		project.paramJsonPath = testJsonPath
		project.apply plugin: 'gapcookbook' // cookbook plugin already loads the jenkins extension
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
//		assertThat(project.prodDeploy.nodes, equalTo(["[testnode01.phx.gapinc.dev, testnode02.phx.gapinc.dev]"]))
		assertThat(project.prodDeploy.yumSourceUrl, equalTo("http://ks64.phx.gapinc.dev/gapSoftware/repoName/devel"))
		assertThat(project.prodDeploy.rpmName, equalTo("rpmName-976.rpm"))
		assertThat(project.prodDeploy.yumDestinationUrl, equalTo("http://ks64.phx.gapinc.com/gapSoftware/repoName/devel"))
    }

    @Test
    void promoteRpmTaskIsAddedToTheProject(){
        taskShouldExist('promoteRpm')
    }

    @Test
    void shouldExecutePromoteRpmTask(){
        def mockRpmTask = new MockFor(PromoteRpmTask)
        def mockPrepareTask = new MockFor(PrepareToPromoteToProductionTask)
        mockPrepareTask.demand.execute {}
        mockRpmTask.demand.execute { }
        mockRpmTask.use {
            project.tasks.getByName('promoteRpm').execute()
        }
    }

    @Test
    void shouldAddPromoteArtifactsToProdTaskToProject(){
        taskShouldExist("promoteArtifactsToProd")
    }

    @Test
    void shouldExecutePromoteArtifactsToProdTask() {
        shouldExecuteTask(project,'promoteArtifactsToProd', PromoteArtifactsToProdTask)
    }

	def taskShouldExist(task) {
		assertThat(project.tasks.findByName(task), notNullValue())
	}
}
