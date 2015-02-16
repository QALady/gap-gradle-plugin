package com.gap.gradle.tasks

import com.gap.cloud.VMMetadata
import com.gap.cloud.VirtualMachineBuilder
import com.gap.cloud.openstack.OpenstackCloudProvider
import com.gap.gradle.exceptions.InvalidPropertyAccessException
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters

import groovy.json.JsonSlurper

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

@RequiredParameters([
    @Require(parameter = 'tenant', description = 'Openstack Tenant'),
    @Require(parameter = 'resourceToDelete', description = 'hostname of the resource to Delete')
])
class DeleteEasyCloudResourceTask extends WatchmenTask {
    
    private static final Log LOGGER = LogFactory.getLog(com.gap.gradle.tasks.DeleteEasyCloudResourceTask);
    
    private Properties globalProperties;
    private Project project;
    private CommanderClient commander;
    private VirtualMachineBuilder virtualMachineBuilder;
    private OpenstackCloudProvider openstackCloudProvider;
    
    DeleteEasyCloudResourceTask(Project project) {
        super(project);
        this.project = project; 
        JsonSlurper jsonSlurper = new JsonSlurper();
        Object jsonObject = jsonSlurper.parseText(new URL(project.gapCloud.jsonUrl).text)
        this.populateGlobalProperties(jsonObject);
        commander = new CommanderClient();
        virtualMachineBuilder = new VirtualMachineBuilder();
        openstackCloudProvider = new OpenstackCloudProvider(this.getOpenstackProperties());
    }
    
    DeleteEasyCloudResourceTask(CommanderClient commander, VirtualMachineBuilder virtualMachineBuilder, OpenstackCloudProvider openstackCloudProvider, Object jsonObject, Project project) {
        super(project);
        this.project = project;
        this.commander = commander;
        this.populateGlobalProperties(jsonObject);
        this.virtualMachineBuilder = virtualMachineBuilder;
        this.openstackCloudProvider = openstackCloudProvider;
    }
    
    def deleteEasyCloudResource() {
        super.validate()        
        Set<String> virtualMachineHostnames = new HashSet<String>();
        globalProperties.get(DeleteEasyCloudResourceTask.Constants.RESOURCE_TO_DELETE).split(',').each { eachResourceToDelete ->
            virtualMachineHostnames.add(eachResourceToDelete);
        }        
        LOGGER.info("*******************************************************************");
        LOGGER.info("Disabling VM as an EC Resource");
        Map<String, String> ecDisableMap = new HashMap<String, String>();
        ecDisableMap.put("resourceDisabled", "true");
        for (String eachResourceToDisable : virtualMachineHostnames) {            
            commander.modifyResource(eachResourceToDisable, ecDisableMap);
        }
        
        LOGGER.info("*******************************************************************");
        LOGGER.info("Beginning Deletion of VMs from Openstack")
        List<VMMetadata> vmMetadatas = new ArrayList<VMMetadata>();        
        vmMetadatas = virtualMachineBuilder.withProvider(openstackCloudProvider).deleteVirtualMachines(virtualMachineHostnames);
        LOGGER.info("*******************************************************************");
        LOGGER.info("ALL VMS that were deleted on Openstack are as follows")
        for (VMMetadata eachVMMetadata : vmMetadatas) {
            LOGGER.info(eachVMMetadata.toString())
        }
        
        LOGGER.info("*******************************************************************");
        LOGGER.info("Deleting VMS as resource from Electric Commander")
        for (String eachResourceToDelete : virtualMachineHostnames) {
            commander.deleteResource(eachResourceToDelete)
        }
        LOGGER.info("*******************************************************************"); 
    }
    
    def deleteEasyECResource() {
        super.validate()
        Set<String> virtualMachineHostnames = new HashSet<String>();
        globalProperties.get(DeleteEasyCloudResourceTask.Constants.RESOURCE_TO_DELETE).split(',').each { eachResourceToDelete ->
            virtualMachineHostnames.add(eachResourceToDelete);
        }
        LOGGER.info("*******************************************************************");
        LOGGER.info("Deleting VMS as resource from Electric Commander")
        for (String eachResourceToDelete : virtualMachineHostnames) {
            commander.deleteResource(eachResourceToDelete)
        }
    }
    
    private void populateGlobalProperties(Object jsonObject) {
        def tenantName;
        LOGGER.info("Reading Json properties");
        globalProperties = new Properties();
        
        if (project.hasProperty('resourceToDelete')) {
            globalProperties.put(DeleteEasyCloudResourceTask.Constants.RESOURCE_TO_DELETE, project[DeleteEasyCloudResourceTask.Constants.RESOURCE_TO_DELETE].replace(" ", ""));
        } else {
            throw new InvalidPropertyAccessException("Resource to Delete property not set in project. Please set project property 'resourceToDelete'");
        }
        
        if (project.hasProperty('tenant')) {
            tenantName = project.tenant;
            if (jsonObject[DeleteEasyCloudResourceTask.Constants.JSON_OBJECT_TENANTS][tenantName] != null) {                
                globalProperties.put(DeleteEasyCloudResourceTask.Constants.OS_USERNAME, jsonObject[DeleteEasyCloudResourceTask.Constants.JSON_OBJECT_TENANTS][tenantName][DeleteEasyCloudResourceTask.Constants.OS_USERNAME]);
                globalProperties.put(DeleteEasyCloudResourceTask.Constants.OS_PASSWORD, jsonObject[DeleteEasyCloudResourceTask.Constants.JSON_OBJECT_TENANTS][tenantName][DeleteEasyCloudResourceTask.Constants.OS_PASSWORD]);
                globalProperties.put(DeleteEasyCloudResourceTask.Constants.OS_ENDPOINT, jsonObject[DeleteEasyCloudResourceTask.Constants.JSON_OBJECT_TENANTS][tenantName][DeleteEasyCloudResourceTask.Constants.OS_ENDPOINT]);
                globalProperties.put(DeleteEasyCloudResourceTask.Constants.OS_TENANT, jsonObject[DeleteEasyCloudResourceTask.Constants.JSON_OBJECT_TENANTS][tenantName][DeleteEasyCloudResourceTask.Constants.OS_TENANT]);                                
            } else {
                throw new InvalidPropertyAccessException("No such tenant " + tenantName + " exists in the cloud. Please check tenant name");
            }
        } else {
             throw new InvalidPropertyAccessException("Tenant property not set in project. Please set project property 'tenant'");
        }        
        /*    for (Map.Entry<String, String> entry : globalProperties.entrySet()) {
         println (entry.getKey() + " ==> " + entry.getValue())
     }*/
    }

    private Properties getOpenstackProperties() {
        Properties properties = new Properties();
        properties.put("username", globalProperties.get(DeleteEasyCloudResourceTask.Constants.OS_USERNAME));
        properties.put("password", globalProperties.get(DeleteEasyCloudResourceTask.Constants.OS_PASSWORD));
        properties.put("endpoint", globalProperties.get(DeleteEasyCloudResourceTask.Constants.OS_ENDPOINT));
        properties.put("tenant", globalProperties.get(DeleteEasyCloudResourceTask.Constants.OS_TENANT));
        return properties;
    }
    
    static class Constants {
        private Constants() {
           
        }        
        public static final String OS_USERNAME = "osUsername";
        public static final String OS_PASSWORD = "osPassword";
        public static final String OS_ENDPOINT = "osEndpoint";
        public static final String OS_TENANT = "osTenant";
        public static final String JSON_OBJECT_TENANTS = "tenants";
        public static final String RESOURCE_TO_DELETE = "resourceToDelete";      
    }
}
