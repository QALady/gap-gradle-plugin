package com.gap.pipeline.tasks

import static junit.framework.Assert.assertEquals
import static junit.framework.Assert.assertTrue
import static org.hamcrest.CoreMatchers.is
import static org.junit.Assert.assertThat
import static org.junit.rules.ExpectedException.none

import com.gap.pipeline.ProdPrepareConfig
import com.gap.pipeline.ec.CommanderArtifacts
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.exception.InvalidSHA1IDException
import groovy.json.JsonSlurper
import groovy.mock.interceptor.MockFor
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.rules.TemporaryFolder

import static matchers.CustomMatchers.sameString

class PrepareForProductionDeployTaskTest {

    def prepareForProductionDeployTask
    def project
    def mockCommanderArtifacts
    def mockCommanderClient
    def mockUploadBuildArtifactsTask

	final static String sha1_1 = "6dc4a1a3748a29ec8a8e46fbdcd22b1e55206999"
	final static String sha1_2 = "87986d3e66cd088804c5cf1b822aa155e1b03f00"
	final static String sha1_3 = "52d1b6c0e1b78ae023db942ce7865f3bc772fb0a"
	final static String cookbookSha1Id_1 = "bae7745b6577b402d946d35587bf629b3814210a"
	@Rule
    public final ExpectedException expectedException = none()

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()
    @Before
    void setUp (){
        project = ProjectBuilder.builder().withProjectDir(new File(temporaryFolder.root.path)).build()
        project.apply plugin: 'gappipeline'
        project.prodPrepare.deployECProcedure="Project:Procedure"
        project.prodPrepare.sha1Ids = "${sha1_1}"
		project.prodPrepare.appVersion = 'default'
        project.artifactCoordinates = "com.gap.sandbox:iso:1234"
		project.prodPrepare.cookbookName = "test-ref-app"
		project.prodPrepare.cookbookSha1Id = cookbookSha1Id_1
        project.prodPrepare.yumSourceUrl = "http://devyum/repo/org/channel"
        project.prodPrepare.yumDestinationUrl = "http://prodyum/repo/org/channel"
        project.prodPrepare.rpmName = "myrpm-default.noarch.rpm"
        project.buildDir = "${temporaryFolder.root.path}/build"
        new File("${temporaryFolder.root.path}/build/artifacts".toString()).mkdirs()
        new File("${temporaryFolder.root.path}/artifacts".toString()).mkdirs()

        prepareForProductionDeployTask = new PrepareForProductionDeployTask(project)
        mockCommanderArtifacts = new MockFor(CommanderArtifacts)
        mockCommanderClient = new MockFor(CommanderClient)
        mockUploadBuildArtifactsTask = new MockFor(UploadBuildArtifactsTask)
    }

    @Test
    void shouldCreateLinksForCommanderArtifactsInCurrentJobDirectory(){
        setupDefaultMocks()
        executeTask()
    }

    @Test
    void shouldNotThrowExceptionIfAllRequiredParametersAreProvided (){
        project.prodPrepare.deployECProcedure = 'project:procedure'
        project.prodPrepare.sha1Ids =sha1_1
        prepareForProductionDeployTask.validate()
    }

    @Test
    void shouldThrowException_whenECProcedureNameToTriggerIsNotProvided (){
        expectedException.expect(Exception)
        expectedException.expectMessage("Missing required parameter: 'prodPrepare.deployECProcedure'")
        project.prodPrepare.deployECProcedure = null
        prepareForProductionDeployTask.validate()
    }

    @Test
    void shouldThrowException_whenShaIdsIsNotProvided (){
        expectedException.expect(Exception)
        expectedException.expectMessage("Missing required parameter: 'prodPrepare.sha1Ids'")
        project.prodPrepare.sha1Ids = null
        prepareForProductionDeployTask.validate()
    }

    @Test
    void shouldCreateArtifactWithInputParameters () {
        project.prodPrepare.deployECProcedure = 'Project:Procedure'
        project.prodPrepare.sha1Ids = sha1_1
        project.prodPrepare.roleName = 'myrole'
        project.prodPrepare.cookbookName = 'mycookbook'
        project.prodPrepare.nodes ="node1,node2"
		project.prodPrepare.appVersion = '12345'
        setupDefaultMocks()

        executeTask()

        def prodDeployFileName = "${project.buildDir}/artifacts/${ProdPrepareConfig.FILE_NAME}"

        def file = new File(prodDeployFileName)
        assertTrue(file.exists())
        def json = new JsonSlurper().parseText(new File(prodDeployFileName).text)
        assertEquals([sha1_1], json.sha1IdList)
        assertEquals('myrole',json.roleName)
        assertEquals('mycookbook', json.cookbook.name)
		assertEquals(cookbookSha1Id_1, json.cookbook.sha1Id)
        assertEquals(['node1','node2'], json.nodes)
		assertEquals('12345', json.appVersion)
    }

	@Test
	void shouldCreateArtifactWithMultipleShaIdInputParameters () {
		project.prodPrepare.deployECProcedure = 'Project:Procedure'
		project.prodPrepare.sha1Ids = "${sha1_1},${sha1_2},${sha1_3}"
		project.prodPrepare.roleName = 'myrole'
		project.prodPrepare.cookbookName = 'mycookbook'
		project.prodPrepare.nodes ="node1,node2"
		setupDefaultMocks()

		executeTask()

		def prodDeployFileName = "${project.buildDir}/artifacts/${ProdPrepareConfig.FILE_NAME}"

		def file = new File(prodDeployFileName)
		assertTrue(file.exists())
		def json = new JsonSlurper().parseText(new File(prodDeployFileName).text)
		assertEquals([sha1_1, sha1_2, sha1_3], json.sha1IdList)
		assertEquals('myrole',json.roleName)
		assertEquals('mycookbook', json.cookbook.name)
		assertEquals(['node1','node2'], json.nodes)
	}

	@Test
	void shouldCreateArtifactWithMultipleShaIdInputParametersTrimmed () {
		project.prodPrepare.deployECProcedure = 'Project:Procedure'
		project.prodPrepare.sha1Ids = " ${sha1_1}, ${sha1_2} ,${sha1_3} "
		project.prodPrepare.roleName = 'myrole'
		project.prodPrepare.cookbookName = 'mycookbook'
		project.prodPrepare.nodes ="node1,node2"
		setupDefaultMocks()

		executeTask()

		def prodDeployFileName = "${project.buildDir}/artifacts/${ProdPrepareConfig.FILE_NAME}"

		def file = new File(prodDeployFileName)
		assertTrue(file.exists())
		def json = new JsonSlurper().parseText(new File(prodDeployFileName).text)
		assertEquals([sha1_1,sha1_2,sha1_3], json.sha1IdList)
		assertEquals('myrole',json.roleName)
		assertEquals('mycookbook', json.cookbook.name)
		assertEquals(['node1','node2'], json.nodes)
	}

	@Test
	void shouldCreateArtifactWithSloppyMultilineMulticommaShaIdInputParametersTrimmed () {
		project.prodPrepare.deployECProcedure = 'Project:Procedure'
		project.prodPrepare.sha1Ids = "${sha1_1},\n${sha1_2}\n,,\n\n${sha1_3}"
		project.prodPrepare.roleName = 'myrole'
		project.prodPrepare.cookbookName = 'mycookbook'
		project.prodPrepare.nodes ="node1,node2"
		setupDefaultMocks()

		executeTask()

		def prodDeployFileName = "${project.buildDir}/artifacts/${ProdPrepareConfig.FILE_NAME}"

		def file = new File(prodDeployFileName)
		assertTrue(file.exists())
		def json = new JsonSlurper().parseText(new File(prodDeployFileName).text)
		assertEquals([sha1_1,sha1_2,sha1_3], json.sha1IdList)
		assertEquals('myrole',json.roleName)
		assertEquals('mycookbook', json.cookbook.name)
		assertEquals(['node1','node2'], json.nodes)
	}

	@Test
	void shouldThrowException_whenCreateArtifactWithInvalidShaIdParameters () {
		String invalidSha1Id = "thisisaninvalidshaid"
		expectedException.expect(InvalidSHA1IDException)
		expectedException.expectMessage("Invalid SHA1 id: ${invalidSha1Id}")
		project.prodPrepare.deployECProcedure = 'Project:Procedure'
		project.prodPrepare.sha1Ids = "${invalidSha1Id} ,${sha1_3} "
		project.prodPrepare.roleName = 'myrole'
		project.prodPrepare.cookbookName = 'mycookbook'
		project.prodPrepare.nodes ="node1,node2"
		mockCommanderArtifacts.demand.publishLinks {}
		mockCommanderClient.demand.runProcedure { }
		mockUploadBuildArtifactsTask.demand.execute {}
		executeTask()
	}

	@Test
	void shouldThrowException_whenCreateArtifactWithInvalidCookbookShaId () {
		String invalidSha1Id = "thisisaninvalidshaid"
		expectedException.expect(InvalidSHA1IDException)
		expectedException.expectMessage("Invalid SHA1 id: ${invalidSha1Id}")
		project.prodPrepare.deployECProcedure = 'Project:Procedure'
		project.prodPrepare.sha1Ids = "${sha1_3}"
		project.prodPrepare.cookbookSha1Id = invalidSha1Id
		project.prodPrepare.roleName = 'myrole'
		project.prodPrepare.cookbookName = 'mycookbook'
		project.prodPrepare.nodes ="node1,node2"
		mockCommanderArtifacts.demand.publishLinks {}
		mockCommanderClient.demand.runProcedure { }
		mockUploadBuildArtifactsTask.demand.execute {}
		executeTask()
	}


	@Test
    void shouldCopyTheProdDeployJsonArtifactToElectricCommandersDefaultArtifactsLocation(){
        mockCommanderArtifacts.demand.copyToArtifactsDir { path ->
            assertThat("${project.buildDir}/artifacts/prodDeployParameters.json", is(path))
        }
        mockCommanderArtifacts.demand.publishLinks {}
        mockCommanderClient.demand.setDefaultParameterValue{procedure, name, value ->}
        mockUploadBuildArtifactsTask.demand.execute {}
        mockCommanderClient.demand.addLinkToRunProcedureInJob{ linkName, procedure -> }
        executeTask()
    }

    @Test
    void shouldInvokeUploadBuildArtifactsTask(){
        setupDefaultMocks()
        executeTask()
    }

    @Test
    void shouldSetDefaultValueOfArtifactCoordinatesInECDeployProcedure(){
        project.prodPrepare.deployECProcedure = 'Project:Procedure'
        project.artifactCoordinates = 'group:name:1234'
        mockCommanderArtifacts.demand.copyToArtifactsDir {}
        mockCommanderArtifacts.demand.publishLinks {}
        mockCommanderClient.demand.setDefaultParameterValue {procedure, name, value ->
            assertThat(procedure, is('Project:Procedure'))
            assertThat(name, is('artifactCoordinates'))
            assertThat(value, is('group:name:1234'))
        }
        mockUploadBuildArtifactsTask.demand.execute {}
        mockCommanderClient.demand.addLinkToRunProcedureInJob{ linkName, procedure -> }

        executeTask()
    }

    @Test
    void shouldAddLinkToECDeployProcedure(){
        project.prodPrepare.deployECProcedure = 'Project:Procedure'
        mockCommanderArtifacts.demand.copyToArtifactsDir {}
        mockCommanderArtifacts.demand.publishLinks {}
        mockCommanderClient.demand.setDefaultParameterValue{procedure, name, value ->}
        mockUploadBuildArtifactsTask.demand.execute {}
        mockCommanderClient.demand.addLinkToRunProcedureInJob{ linkName, procedure ->
            assertThat(linkName, is(matchers.CustomMatchers.sameString('Run Project:Procedure')))
            assertThat(procedure, is('Project:Procedure'))
        }

        executeTask()
    }

    private void setupDefaultMocks() {
        mockCommanderArtifacts.demand.copyToArtifactsDir {}
        mockCommanderArtifacts.demand.publishLinks {}
        mockCommanderClient.demand.setDefaultParameterValue{procedure, name, value ->}
        mockUploadBuildArtifactsTask.demand.execute {}
        mockCommanderClient.demand.addLinkToRunProcedureInJob{ linkName, procedure -> }
    }

    private void executeTask() {
        mockCommanderArtifacts.use {
            mockCommanderClient.use {
                mockUploadBuildArtifactsTask.use {
                    prepareForProductionDeployTask.execute()
                }
            }
        }
    }



}
