package com.gap.gradle.tasks

import com.gap.cloud.VMMetadata;
import com.gap.cloud.VirtualMachineBuilder;
import com.gap.cloud.openstack.OpenstackCloudProvider;
import com.gap.gradle.exceptions.InvalidPropertyAccessException;
import com.gap.gradle.extensions.GapCloudResource;
import com.gap.pipeline.ec.CommanderClient;
import com.github.tomakehurst.wiremock.junit.WireMockRule

import groovy.json.JsonSlurper;

import java.util.Set;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.jclouds.domain.Location
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.security.CodeSource

class CreateEasyCloudResourceTaskTest {
    
    private Project project;
    private CommanderClient commander;
    private VirtualMachineBuilder virtualMachineBuilder;
    private OpenstackCloudProvider openstackCloudProvider;
    
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8088);      
   
    @Test
    public void createCloudResourcePassEtcdTest() {        
        this.setUpProject();
        this.setUpECProperties();
        this.setUpWebServer();
        this.setVirtualMachineBuilder();
        project.ext.createResource = 'TRuE';
        project.ext.pool = 'watchmen-test-pool';
        CreateEasyCloudResourceTask createEasyCloudResourceTask = new CreateEasyCloudResourceTask(commander, virtualMachineBuilder, openstackCloudProvider, this.getJsonObject(), project);
        createEasyCloudResourceTask.createEasyCloudResource();
        assertEquals(2, createEasyCloudResourceTask.successfullyRegisteredInstances.size());
        verify(commander).setECProperty("/myJob/osResources/watchmen-test-group-ace/id", "fab3501d-aa16-43a8-866d-4e2d77a4c5ff");
        verify(commander).setECProperty("/myJob/osResources/watchmen-test-group-fgh/id", "abc3501d-aa16-43a8-866d-4e2d77a4cabc");
        verify(commander).setECProperty("/myJob/osResources/watchmen-test-group-ace/ip", "10.9.59.168");
        verify(commander).setECProperty("/myJob/osResources/watchmen-test-group-fgh/ip", "10.9.59.100");
        verify(commander).setECProperty("/myJob/osResources/watchmen-test-group-ace/image", "rhel-6.4-r12-corp-tdev-prebaked-041120140926");
        verify(commander).setECProperty("/myJob/osResources/watchmen-test-group-fgh/image", "rhel-6.4-r12-corp-tdev-prebaked-041120140926");
        assertEquals(2, project.gapCloud.allInstances.size());
        assertEquals(2, project.gapCloud.allRegisteredInstances.size())
    }
    
    @Test
    public void createCloudResourceFailEtcdTest() {
        this.setUpProject();
        this.setUpECProperties();
        this.setUpWebServer();
        this.setVirtualMachineBuilder();
        project.ext.etcdHostnameUrl = "http://localhost:8099/v2/keys"
        CreateEasyCloudResourceTask createEasyCloudResourceTask = new CreateEasyCloudResourceTask(commander, virtualMachineBuilder, openstackCloudProvider, this.getJsonObject(), project);
        createEasyCloudResourceTask.createEasyCloudResource();
        assertEquals(0, createEasyCloudResourceTask.successfullyRegisteredInstances.size());
        assertEquals(2, project.gapCloud.allInstances.size());
        assertEquals(0, project.gapCloud.allRegisteredInstances.size())
    }
    
    @Test
    public void validTypeProjectPropertyTest() {
        this.setUpProject();
        this.setUpECProperties();        
        this.setVirtualMachineBuilder();
        project.ext.type = 'build'
        CreateEasyCloudResourceTask createEasyCloudResourceTask = new CreateEasyCloudResourceTask(commander, virtualMachineBuilder, openstackCloudProvider, this.getJsonObject(), project);
        assertEquals("linux-build-dna", createEasyCloudResourceTask.globalProperties.get(CreateEasyCloudResourceTask.Constants.DNATYPE));
        assertEquals("rhel-6.6-r3", createEasyCloudResourceTask.globalProperties.get(CreateEasyCloudResourceTask.Constants.OS_IMAGENAME));
        assertEquals("600", createEasyCloudResourceTask.globalProperties.get(CreateEasyCloudResourceTask.Constants.PHONE_TIMEOUT));
    }
    
    @Test(expected = InvalidPropertyAccessException)
    public void invalidTypeProjectPropertyExceptionTest() {
        this.setUpProject();
        this.setUpECProperties();        
        this.setVirtualMachineBuilder();
        project.ext.type = 'buil'
        CreateEasyCloudResourceTask createEasyCloudResourceTask = new CreateEasyCloudResourceTask(commander, virtualMachineBuilder, openstackCloudProvider, this.getJsonObject(), project);               
    }
    
    @Test
    public void validTenantProjectPropertyTest() {
        this.setUpProject();
        this.setUpECProperties();
        this.setVirtualMachineBuilder();
        project.ext.tenant = 'watchmen_px'
        CreateEasyCloudResourceTask createEasyCloudResourceTask = new CreateEasyCloudResourceTask(commander, virtualMachineBuilder, openstackCloudProvider, this.getJsonObject(), project);
        assertEquals("tdev", createEasyCloudResourceTask.globalProperties.get(CreateEasyCloudResourceTask.Constants.OS_AVAILABILITY_ZONE));
        assertEquals("https://pxcloud.gid.gap.com:5000/v2.0", createEasyCloudResourceTask.globalProperties.get(CreateEasyCloudResourceTask.Constants.OS_ENDPOINT));
        assertEquals("Watchmen", createEasyCloudResourceTask.globalProperties.get(CreateEasyCloudResourceTask.Constants.OS_KEYNAME));
        assertEquals("blah", createEasyCloudResourceTask.globalProperties.get(CreateEasyCloudResourceTask.Constants.OS_PASSWORD));
        assertEquals("watchmen_px", createEasyCloudResourceTask.globalProperties.get(CreateEasyCloudResourceTask.Constants.OS_TENANT));
        assertEquals("blah", createEasyCloudResourceTask.globalProperties.get(CreateEasyCloudResourceTask.Constants.OS_USERNAME));
    }
    
    @Test(expected = InvalidPropertyAccessException)
    public void invalidTenantProjectPropertyExceptionTest() {
        this.setUpProject();
        this.setUpECProperties();
        this.setVirtualMachineBuilder();
        project.ext.tenant = 'watchmen'
        CreateEasyCloudResourceTask createEasyCloudResourceTask = new CreateEasyCloudResourceTask(commander, virtualMachineBuilder, openstackCloudProvider, this.getJsonObject(), project);
    }
    
    @Test
    public void validNetworkProjectPropertyTest() {
        this.setUpProject();
        this.setUpECProperties();
        this.setVirtualMachineBuilder();
        project.ext.network = 'private'
        CreateEasyCloudResourceTask createEasyCloudResourceTask = new CreateEasyCloudResourceTask(commander, virtualMachineBuilder, openstackCloudProvider, this.getJsonObject(), project);
        assertEquals("ISO", createEasyCloudResourceTask.globalProperties.get(CreateEasyCloudResourceTask.Constants.OS_NETWORK_ECZONE));
        assertEquals("iso-private", createEasyCloudResourceTask.globalProperties.get(CreateEasyCloudResourceTask.Constants.OS_NETWORKNAME));        
    }
    
    @Test(expected = InvalidPropertyAccessException)
    public void invalidNetworkProjectPropertyExceptionTest() {
        this.setUpProject();
        this.setUpECProperties();
        this.setVirtualMachineBuilder();
        project.ext.network = 'publi'
        CreateEasyCloudResourceTask createEasyCloudResourceTask = new CreateEasyCloudResourceTask(commander, virtualMachineBuilder, openstackCloudProvider, this.getJsonObject(), project);
    }
    
    @Test
    public void inFluxDBProjectPropertyTest() {
        this.setUpProject();
        this.setUpECProperties();
        this.setVirtualMachineBuilder();
        project.ext.inFluxDBUrl = 'blahInflux'
        CreateEasyCloudResourceTask createEasyCloudResourceTask = new CreateEasyCloudResourceTask(commander, virtualMachineBuilder, openstackCloudProvider, this.getJsonObject(), project);
        assertEquals("blahInflux", createEasyCloudResourceTask.globalProperties.get(CreateEasyCloudResourceTask.Constants.INFLUX_DB_URL));        
    }
    
    @Test
    public void osFlavorProjectPropertyTest() {
        this.setUpProject();
        this.setUpECProperties();
        this.setVirtualMachineBuilder();
        project.ext.osFlavor = 'blahOsFlavor'
        CreateEasyCloudResourceTask createEasyCloudResourceTask = new CreateEasyCloudResourceTask(commander, virtualMachineBuilder, openstackCloudProvider, this.getJsonObject(), project);
        assertEquals("blahOsFlavor", createEasyCloudResourceTask.globalProperties.get(CreateEasyCloudResourceTask.Constants.OS_FLAVOR));
    }
    
    @Test
    public void numberOfInstancesProjectPropertyTest() {
        this.setUpProject();
        this.setUpECProperties();
        this.setVirtualMachineBuilder();
        project.ext.numberOfInstances = '10'
        CreateEasyCloudResourceTask createEasyCloudResourceTask = new CreateEasyCloudResourceTask(commander, virtualMachineBuilder, openstackCloudProvider, this.getJsonObject(), project);
        assertEquals("10", createEasyCloudResourceTask.globalProperties.get(CreateEasyCloudResourceTask.Constants.NUMBER_OF_INSTANCES));
    }
    
    @Test
    public void dnaUrlProjectPropertyTest() {
        this.setUpProject();
        this.setUpECProperties();
        this.setVirtualMachineBuilder();
        project.ext.DNAURL = 'blahDNAURL'
        CreateEasyCloudResourceTask createEasyCloudResourceTask = new CreateEasyCloudResourceTask(commander, virtualMachineBuilder, openstackCloudProvider, this.getJsonObject(), project);
        assertEquals("blahDNAURL", createEasyCloudResourceTask.globalProperties.get(CreateEasyCloudResourceTask.Constants.DNAURL));
    }
    
    @Test
    public void etcdHostnameUrlProjectPropertyTest() {
        this.setUpProject();
        this.setUpECProperties();
        this.setVirtualMachineBuilder();
        project.ext.etcdHostnameUrl = 'blahetcdURL'
        CreateEasyCloudResourceTask createEasyCloudResourceTask = new CreateEasyCloudResourceTask(commander, virtualMachineBuilder, openstackCloudProvider, this.getJsonObject(), project);
        assertEquals("blahetcdURL", createEasyCloudResourceTask.globalProperties.get(CreateEasyCloudResourceTask.Constants.ETCD_HOSTNAME_URL));
    }
    
    @Test
    public void etcdKeyProjectPropertyTest() {
        this.setUpProject();
        this.setUpECProperties();
        this.setVirtualMachineBuilder();
        project.ext.etcdKey = 'blahetcdKey'
        CreateEasyCloudResourceTask createEasyCloudResourceTask = new CreateEasyCloudResourceTask(commander, virtualMachineBuilder, openstackCloudProvider, this.getJsonObject(), project);
        assertEquals("blahetcdKey", createEasyCloudResourceTask.globalProperties.get(CreateEasyCloudResourceTask.Constants.ETCD_KEY));
    }
    
    @Test
    public void osSecurityGroupSetProjectPropertyTest() {
        this.setUpProject();
        this.setUpECProperties();
        this.setVirtualMachineBuilder();
        project.ext.osSecurityGroupSet = 'blahsec1, blahsec2, blahsec3';
        CreateEasyCloudResourceTask createEasyCloudResourceTask = new CreateEasyCloudResourceTask(commander, virtualMachineBuilder, openstackCloudProvider, this.getJsonObject(), project);
        assertEquals("blahsec1,blahsec2,blahsec3", createEasyCloudResourceTask.globalProperties.get(CreateEasyCloudResourceTask.Constants.OS_SECURITY_GROUP_SET));
    }
    
    @Test
    public void roleNameProjectPropertyTest() {
        this.setUpProject();
        this.setUpECProperties();
        this.setVirtualMachineBuilder();
        project.ext.roleName = 'blahrolename';
        CreateEasyCloudResourceTask createEasyCloudResourceTask = new CreateEasyCloudResourceTask(commander, virtualMachineBuilder, openstackCloudProvider, this.getJsonObject(), project);
        assertEquals("blahrolename", createEasyCloudResourceTask.globalProperties.get(CreateEasyCloudResourceTask.Constants.ROLE_NAME));
    }
    
    @Test
    public void autoPurgeProjectPropertyTest() {
        this.setUpProject();
        this.setUpECProperties();
        this.setVirtualMachineBuilder();
        project.ext.autoPurge = 'false';
        CreateEasyCloudResourceTask createEasyCloudResourceTask = new CreateEasyCloudResourceTask(commander, virtualMachineBuilder, openstackCloudProvider, this.getJsonObject(), project);
        assertEquals("false", createEasyCloudResourceTask.globalProperties.get(CreateEasyCloudResourceTask.Constants.AUTO_PURGE));
    }
    
    @Test
    public void createECResourceProjectPropertyTest() {
        this.setUpProject();
        this.setUpECProperties();
        this.setVirtualMachineBuilder();
        project.ext.createResource = 'true';
        project.ext.pool = "watchmen-test-pool";
        CreateEasyCloudResourceTask createEasyCloudResourceTask = new CreateEasyCloudResourceTask(commander, virtualMachineBuilder, openstackCloudProvider, this.getJsonObject(), project);
        assertEquals("true", createEasyCloudResourceTask.globalProperties.get(CreateEasyCloudResourceTask.Constants.CREATE_EC_RESOURCE_FLAG));
        assertEquals("watchmen-test-pool", createEasyCloudResourceTask.globalProperties.get(CreateEasyCloudResourceTask.Constants.POOL_NAME));
    }
    
    private void setUpWebServer() {
        String etcdResponse = "{\"action\":\"get\",\"node\":{\"key\":\"/iso/create-vm\",\"dir\":,\"nodes\":[{\"key\":\"/iso/create-vm/fab3501d-aa16-43a8-866d-4e2d77a4c5ff\",\"dir\":,\"modifiedIndex\":305842,\"createdIndex\":305842}],\"modifiedIndex\":2,\"createdIndex\":2}}";
        
        stubFor(get(urlEqualTo("/pipeline-dna/corp-prebaked-dynamic-dna"))
            .willReturn(aResponse().withStatus(200).withBody("User data String")))
       
         stubFor(get(urlEqualTo("/v2/keys/iso/create-vm/fab3501d-aa16-43a8-866d-4e2d77a4c5ff?wait=true"))
            .willReturn(aResponse().withStatus(200).withBody(etcdResponse)));
        
        stubFor(get(urlEqualTo("/v2/keys/iso/create-vm/abc3501d-aa16-43a8-866d-4e2d77a4cabc?wait=true"))
            .willReturn(aResponse().withStatus(200).withBody(etcdResponse)));
        
        stubFor(get(urlEqualTo("/db/pipeline/series?u=root&p=root"))
            .willReturn(aResponse().withStatus(200).withBody("Written to Influx DB")));
    }
    
    private void setUpECProperties() {
        commander = mock(CommanderClient)        
            
        when(commander.getECProperty("/myJobStep/jobStepId"))
            .thenReturn("mohsin-job-step-id")
        
        when(commander.getECProperty("/myJob/jobName"))
            .thenReturn("mohsin-job-name")
            
        when(commander.getJobId())
            .thenReturn("mohsin-ec-job")
    }
    
    private void setUpProject() {
        project = ProjectBuilder.builder().withName("gapCloudResource").build()
        project.group = 'com.gap.watchmen'
        project.extensions.create("gapCloud", GapCloudResource)
        project.ext.hostname = 'watchmen-test-group'
        project.ext.tenant = 'watchmen_sf'
        project.ext.roleName = 'wm-build-resource'
        project.ext.numberOfInstances = '2'
    }
    
    private Object getJsonObject() {
        JsonSlurper jsonSlurper = new JsonSlurper();
        Object jsonObject = jsonSlurper.parseText(new File("src/test/groovy/com/gap/gradle/resources/gapCloudMock.json").text)
        return jsonObject
    }
    
    private void setVirtualMachineBuilder() {
        Set<String> osNetworkNameSet, osSecurityGroupSet;
        StringBuilder sb = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();
        osNetworkNameSet = new HashSet<String>();
        osSecurityGroupSet = new HashSet<String>();
        Map<String, String> userMetadata = new HashMap<String, String>();
        List<VMMetadata> vmMetadatas = new ArrayList<VMMetadata>();
        Set<String> publicAddresses = new HashSet<String>();
        Set<String> privateAddresses = new HashSet<String>();
        Set<String> privateAddresses1 = new HashSet<String>();
        userMetadata.put("roleName", "wm-build-resource");
        osNetworkNameSet.add("vlan602");
        osSecurityGroupSet.add("default");
        privateAddresses.addAll(Arrays.asList("10.9.59.168"));
        privateAddresses1.addAll(Arrays.asList("10.9.59.100"));
        openstackCloudProvider = mock(OpenstackCloudProvider.class);
        virtualMachineBuilder = mock(VirtualMachineBuilder.class, RETURNS_DEEP_STUBS);
        Location location = mock(Location.class);
        VMMetadata vmMetadata = mock(VMMetadata.class);
        VMMetadata vmMetadata1 = mock(VMMetadata.class);
        
        when(vmMetadata.getUserMetadata())
            .thenReturn(userMetadata);
        when(vmMetadata.getNodeId())
            .thenReturn("RegionOne/fab3501d-aa16-43a8-866d-4e2d77a4c5ff");
        when(vmMetadata.getProviderId())
            .thenReturn("fab3501d-aa16-43a8-866d-4e2d77a4c5ff");
        when(vmMetadata.getHostname())
            .thenReturn("watchmen-test-group-ace");
        when(vmMetadata.getGroupName())
            .thenReturn("watchmen-test-group");
        when(vmMetadata.getPublicAddresses())
            .thenReturn(publicAddresses);
        when(vmMetadata.getPrivateAddresses())
            .thenReturn(privateAddresses);
        when(vmMetadata.getLocation())
            .thenReturn(location);
        when(location.getId())
            .thenReturn("e03e8d12157b51c0e7c6b7ef59ae2115869d7e9b852864d1ee07b632");
        when(vmMetadata.getFloatingIPAddress())
            .thenReturn(null);
        sb.append("{ \"ID\" : \""  + vmMetadata.getNodeId()).append("\", \"ProviderId\" : \"" + vmMetadata.getProviderId())
                .append("\", \"Hostname\" : \"" + vmMetadata.getHostname())
                .append("\", \"Group Name\" : \"" + vmMetadata.getGroupName())
                .append("\", \"Public Addresses\" : \"" + vmMetadata.getPublicAddresses())
                .append("\", \"Private Addresse\" : \"" + vmMetadata.getPrivateAddresses())
                .append("\", \"Floating IP Address\" : \"" + vmMetadata.getFloatingIPAddress())
                .append("\", \"User Metadata\" : \"" + vmMetadata.getUserMetadata())
                .append("\", \"Host ID\" : \"" + vmMetadata.getLocation().getId() + "\"}");
        when(vmMetadata.toString())
            .thenReturn(sb.toString())
        
        when(vmMetadata1.getUserMetadata())
            .thenReturn(userMetadata);
        when(vmMetadata1.getNodeId())
            .thenReturn("RegionOne/abc3501d-aa16-43a8-866d-4e2d77a4cabc");
        when(vmMetadata1.getProviderId())
            .thenReturn("abc3501d-aa16-43a8-866d-4e2d77a4cabc");
        when(vmMetadata1.getHostname())
            .thenReturn("watchmen-test-group-fgh");
        when(vmMetadata1.getGroupName())
            .thenReturn("watchmen-test-group");
        when(vmMetadata1.getPublicAddresses())
            .thenReturn(publicAddresses);
        when(vmMetadata1.getPrivateAddresses())
            .thenReturn(privateAddresses1);
        when(vmMetadata1.getLocation())
            .thenReturn(location);
        when(location.getId())
            .thenReturn("e03e8d12157b51c0e7c6b7ef59ae2115869d7e9b852864d1ee07b632")
        when(vmMetadata1.getFloatingIPAddress())
            .thenReturn(null);
        sb1.append("{ \"ID\" : \""  + vmMetadata1.getNodeId()).append("\", \"ProviderId\" : \"" + vmMetadata1.getProviderId())
                .append("\", \"Hostname\" : \"" + vmMetadata1.getHostname())
                .append("\", \"Group Name\" : \"" + vmMetadata1.getGroupName())
                .append("\", \"Public Addresses\" : \"" + vmMetadata1.getPublicAddresses())
                .append("\", \"Private Addresses\" : \"" + vmMetadata1.getPrivateAddresses())
                .append("\", \"Floating IP Address\" : \"" + vmMetadata1.getFloatingIPAddress())
                .append("\", \"User Metadata\" : \"" + vmMetadata1.getUserMetadata())
                .append("\", \"Host ID\" : \"" + vmMetadata1.getLocation().getId() + "\"}");
        when(vmMetadata1.toString())
            .thenReturn(sb1.toString())
            
        
        vmMetadatas.add(vmMetadata);
        vmMetadatas.add(vmMetadata1);
        
        when(virtualMachineBuilder.withProvider(openstackCloudProvider)
                                  .withGroupName("watchmen-test-group")
                                  .withHardwareProfileNameRegex("m1.medium")
                                  .withOsImageRegex("rhel-6.4-r12-corp-tdev-prebaked-041120140926")
                                  .withNetworks(osNetworkNameSet)
                                  .withSecurityGroups(osSecurityGroupSet)
                                  .withKeypairName("root_at_isolated")
                                  .withUserData("User data String")
                                  .withUserMetadata(userMetadata)
                                  .withOsAvailabilityZone("nova"))
            .thenReturn(virtualMachineBuilder);
       
        when(virtualMachineBuilder.buildVirtualMachine(2))
            .thenReturn(vmMetadatas);
    }
}
