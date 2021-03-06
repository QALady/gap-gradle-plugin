package com.gap.gradle.plugins

import com.gap.gradle.plugins.cookbook.ConfigFileResource
import com.gap.gradle.tasks.PromoteRpmsTask
import com.gap.pipeline.tasks.GenerateAuditReportTask
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.notNullValue
import static org.junit.Assert.*

class GapProdDeployPluginTest {

	@Rule
	public final ConfigFileResource config = new ConfigFileResource(GapProdDeployPlugin, "CONFIG_FILE")

	private Project project
	private static String pluginName = 'gapproddeploy'
	private static String testJsonPath = "src/test/groovy/com/gap/gradle/resources/"

	@Before
	void setup() {
		project = ProjectBuilder.builder().build()
		project.metaClass.paramJsonPath = testJsonPath
		project.metaClass.ecUser = "testuser"
		project.apply plugin: pluginName
	}

	@Test
	void deployToProdDeployTaskIsAddedToProject(){
		taskShouldExist('deployToProduction')
	}

	@Test
	void promoteChefObjectsToProductionDeployTaskIsAddedToProject(){
		taskShouldExist('promoteChefObjectsToProduction')
	}

	@Test
	void promoteCookbookToProdChefServerIsAddedToProject() {
		taskShouldExist('promoteCookbookToProdChefServer')	
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
		project.metaClass.paramJsonPath = testJsonPath
		project.metaClass.ecUser = "testuser"
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
		assertThat(project.prodDeploy.nodes, equalTo("[testnode01.phx.gapinc.dev,testnode02.phx.gapinc.dev]"))
		assertThat(project.rpm.yumSourceUrl, equalTo("http://ks64.phx.gapinc.dev/gapSoftware/repoName/devel"))
		assertThat(project.rpm.rpmNames, equalTo(["rpmNames-976.rpm"]))
		assertThat(project.rpm.yumDestinationUrl, equalTo("http://ks64.phx.gapinc.com/gapSoftware/repoName/devel"))
    }

    @Test
    void promoteRpmsTaskIsAddedToTheProject(){
        taskShouldExist('promoteRpms')
    }

	@Test
	void promoteCookbookBerksfileIsAddedToTheProject() {
		taskShouldExist('promoteCookbookBerksfile')
	}

    @Test
    void setupBuildDirsTaskIsAddedToTheProject (){
        taskShouldExist('setupProdBuildDirectories')
    }

    @Test
    void generateAuditReportTaskIsAddedToTheProject (){
        taskShouldExist('generateAuditReport')
    }

    @Test
    void shouldExecuteGenerateAuditReportTask() {
        def mockTask = new MockFor(GenerateAuditReportTask)
        mockTask.demand.execute {}
        def task = project.tasks.findByName('generateAuditReport')
        mockTask.use {
            task.execute()
        }
    }

    @Test
    void shouldExecutePromoteRpmsTask(){
        def mockRpmsTask = new MockFor(PromoteRpmsTask)
        mockRpmsTask.demand.execute { }
        mockRpmsTask.use {
            project.tasks.getByName('promoteRpms').execute()
        }
    }
	
	@Test
	void promoteChefObjectsToProduction_shouldDependOn_promoteCookbookToProdChefServer() {
		taskShouldDependOn('promoteChefObjectsToProduction', 'promoteCookbookToProdChefServer')
	}

	void promoteCookbookToProdChefServer_shouldDependOn_promoteCookbookBerksfile() {
		taskShouldDependOn('promoteCookbookToProdChefServer', 'promoteCookbookBerksfile')
	}

    @Test
    void generateCookbookMetadata_shouldDependOnRequireCookbookValidation() {
        taskShouldDependOn('generateCookbookMetadata', 'requireCookbookValidation')
    }

    @Test
    void requireCookbookValidation_shouldEnableCookbookValidationFlags() {
        project.tasks.findByName('requireCookbookValidation').execute()
        assertTrue(project.chef.requirePinnedDependencies)
        assertTrue(project.chef.requireTransitiveDependencies)
    }

    def taskShouldDependOn(task, requiredDependency) {
        for (def dependency : project.tasks.findByName(task).dependsOn) {
            if (dependency == requiredDependency) {
                return
            } else if (dependency instanceof List) {
                for (def d : dependency) {
                    if (d == requiredDependency) {
                        return
                    }
                }
            }
        }
        fail("Task ${task} does not declare a dependency on ${requiredDependency}")
    }

	def taskShouldExist(task) {
		assertThat(project.tasks.findByName(task), notNullValue())
	}

}
