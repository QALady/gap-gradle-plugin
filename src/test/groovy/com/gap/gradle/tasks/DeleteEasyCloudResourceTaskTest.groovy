package com.gap.gradle.tasks

import com.gap.cloud.VMMetadata
import com.gap.cloud.VirtualMachineBuilder
import com.gap.cloud.openstack.OpenstackCloudProvider
import com.gap.gradle.exceptions.InvalidPropertyAccessException
import com.gap.gradle.extensions.GapCloudResource
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.exception.MissingParameterException
import groovy.json.JsonSlurper

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test

import static org.mockito.Mockito.*


class DeleteEasyCloudResourceTaskTest {
    
    private Project project;
    private CommanderClient commander;
    private VirtualMachineBuilder virtualMachineBuilder;
    private OpenstackCloudProvider openstackCloudProvider;
    
    @Test(expected = InvalidPropertyAccessException)
    public void noTenantProjectPropertyExceptionTest() {
        this.setUpProject()
        this.setUpECProperties()
        this.setVirtualMachineBuilder()
        project.ext.resourceToDelete = 'watchmen-test-group-fgh, watchmen-test-group-ace'
        DeleteEasyCloudResourceTask deleteEasyCloudResourceTask = new DeleteEasyCloudResourceTask(commander, virtualMachineBuilder, openstackCloudProvider, this.getJsonObject(), project);
    }
    
    @Test(expected = InvalidPropertyAccessException)
    public void noResoureProjectPropertyExceptionTest() {
        this.setUpProject()
        this.setUpECProperties()
        this.setVirtualMachineBuilder()
        project.ext.tenant = 'watchmen_sf'
        DeleteEasyCloudResourceTask deleteEasyCloudResourceTask = new DeleteEasyCloudResourceTask(commander, virtualMachineBuilder, openstackCloudProvider, this.getJsonObject(), project);
    }

    @Test(expected = MissingParameterException)
    public void encryptedCredentialsWithoutPasswordSetTest() {
        this.setUpEncryptedCredentialsProject();
        this.setUpECProperties();
        this.setVirtualMachineBuilder();
        project.ext.resourceToDelete = 'watchmen-test-group-ace,watchmen-test-group-fgh'
        DeleteEasyCloudResourceTask deleteEasyCloudResourceTask = new DeleteEasyCloudResourceTask(commander, this.getJsonObject(), project);
    }
    
    @Test
    public void deleteEasyCloudResourceTest() {
        this.setUpProject()
        this.setUpECProperties()
        this.setVirtualMachineBuilder()
        Map<String, String> ecDisableMap = new HashMap<String, String>();
        ecDisableMap.put("resourceDisabled", "true");
        project.ext.tenant = 'watchmen_sf'
        project.ext.resourceToDelete = 'watchmen-test-group-ace,watchmen-test-group-fgh'
        DeleteEasyCloudResourceTask deleteEasyCloudResourceTask = new DeleteEasyCloudResourceTask(commander, virtualMachineBuilder, openstackCloudProvider, this.getJsonObject(), project);
        deleteEasyCloudResourceTask.deleteEasyCloudResource();
        verify(commander).modifyResource('watchmen-test-group-ace', ecDisableMap)
        verify(commander).modifyResource('watchmen-test-group-fgh', ecDisableMap)
        verify(commander).deleteResource('watchmen-test-group-ace')
        verify(commander).deleteResource('watchmen-test-group-fgh')
    }
    
    @Test
    public void deleteEasyECResourceTest() {
        this.setUpProject()
        this.setUpECProperties()
        this.setVirtualMachineBuilder()
        project.ext.tenant = 'watchmen_sf'
        project.ext.resourceToDelete = 'watchmen-test-group-ace,watchmen-test-group-fgh'
        DeleteEasyCloudResourceTask deleteEasyCloudResourceTask = new DeleteEasyCloudResourceTask(commander, virtualMachineBuilder, openstackCloudProvider, this.getJsonObject(), project);
        deleteEasyCloudResourceTask.deleteEasyECResource();
        verify(commander).deleteResource('watchmen-test-group-ace')
        verify(commander).deleteResource('watchmen-test-group-fgh')
    }
    
    private void setUpProject() {
        project = ProjectBuilder.builder().withName("gapCloudResource").build()
        project.group = 'com.gap.watchmen'   
    }

    private void setUpEncryptedCredentialsProject() {
        project = ProjectBuilder.builder().withName("gapCloudResource").build()
        project.group = 'com.gap.watchmen'
        project.extensions.create("gapCloud", GapCloudResource)
        project.ext.hostname = 'watchmen-test-group'
        project.ext.tenant = 'watchmen_sf_actual'
        project.ext.roleName = 'wm-build-resource'
        project.ext.numberOfInstances = '2'
    }
    
    private Object getJsonObject() {
        JsonSlurper jsonSlurper = new JsonSlurper();
        Object jsonObject = jsonSlurper.parseText(new File("src/test/groovy/com/gap/gradle/resources/gapCloudMock.json").text)
        return jsonObject
    }
    
    private void setUpECProperties() {
        commander = mock(CommanderClient)        
    }
    
    private void setVirtualMachineBuilder() {
        Set<String> virtualMachineHostnames = new HashSet<String>();
        virtualMachineHostnames.add("watchmen-test-group-ace");
        virtualMachineHostnames.add("watchmen-test-group-fgh")
        StringBuilder sb = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();
        
        Map<String, String> userMetadata = new HashMap<String, String>();
        List<VMMetadata> vmMetadatas = new ArrayList<VMMetadata>();
        Set<String> publicAddresses = new HashSet<String>();
        Set<String> privateAddresses = new HashSet<String>();
        Set<String> privateAddresses1 = new HashSet<String>();
        userMetadata.put("roleName", "wm-build-resource");
        
        privateAddresses.addAll(Arrays.asList("10.9.59.168"));
        privateAddresses1.addAll(Arrays.asList("10.9.59.100"));
        openstackCloudProvider = mock(OpenstackCloudProvider.class);
        virtualMachineBuilder = mock(VirtualMachineBuilder.class, RETURNS_DEEP_STUBS);
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
        when(vmMetadata.getFloatingIPAddress())
            .thenReturn(null);
        sb.append("{ ID - "  + vmMetadata.getNodeId()).append("; ProviderId - " + vmMetadata.getProviderId())
                .append("; Hostname - " + vmMetadata.getHostname())
                .append("; Group Name - " + vmMetadata.getGroupName())
                .append("; Public Addresses - " + vmMetadata.getPublicAddresses())
                .append("; Private Addresses - " + vmMetadata.getPrivateAddresses())
                .append("; Floating IP Address - " + vmMetadata.getFloatingIPAddress())
                .append("; User Metadata - " + vmMetadata.getUserMetadata());
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
        when(vmMetadata1.getFloatingIPAddress())
            .thenReturn(null);
        sb1.append("{ ID - "  + vmMetadata1.getNodeId()).append("; ProviderId - " + vmMetadata1.getProviderId())
                .append("; Hostname - " + vmMetadata1.getHostname())
                .append("; Group Name - " + vmMetadata1.getGroupName())
                .append("; Public Addresses - " + vmMetadata1.getPublicAddresses())
                .append("; Private Addresses - " + vmMetadata1.getPrivateAddresses())
                .append("; Floating IP Address - " + vmMetadata1.getFloatingIPAddress())
                .append("; User Metadata - " + vmMetadata1.getUserMetadata());
        when(vmMetadata1.toString())
            .thenReturn(sb1.toString())            
        
        vmMetadatas.add(vmMetadata);
        vmMetadatas.add(vmMetadata1);
        
        when(virtualMachineBuilder.withProvider(openstackCloudProvider))
                                  .thenReturn(virtualMachineBuilder);
       
        when(virtualMachineBuilder.deleteVirtualMachines(virtualMachineHostnames))
            .thenReturn(vmMetadatas);
    }
}
