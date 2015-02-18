package com.gap.gradle.tasks

import com.gap.cloud.VMMetadata;
import com.gap.cloud.VirtualMachineBuilder;
import com.gap.cloud.openstack.OpenstackCloudProvider;
import com.gap.gradle.exceptions.InvalidPropertyAccessException
import com.gap.gradle.threads.CreateECResourceThread
import com.gap.gradle.threads.PostVMCreationThread
import com.gap.pipeline.ec.CommanderClient;
import com.gap.pipeline.tasks.WatchmenTask;
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters

import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

@RequiredParameters([
    @Require(parameter = 'tenant', description = 'Openstack Tenant'),
    @Require(parameter = 'hostname', description = 'hostname of the instance')
])
class CreateEasyCloudResourceTask extends WatchmenTask {
        
    private static final Log LOGGER = LogFactory.getLog(com.gap.gradle.tasks.CreateEasyCloudResourceTask);
    private static final int VM_CUSTOMIZATION_THREADS = 10;
   
    private Properties globalProperties;
    private CommanderClient commander;    
    private Project project;
    private VirtualMachineBuilder virtualMachineBuilder;
    private OpenstackCloudProvider openstackCloudProvider;
    private List<VMMetadata> successfullyRegisteredInstances

    CreateEasyCloudResourceTask(Project project) {
        super(project);        
        this.project = project;
        JsonSlurper jsonSlurper = new JsonSlurper();
        Object jsonObject = jsonSlurper.parseText(new URL(project.gapCloud.jsonUrl).text)
        this.populateGlobalProperties(jsonObject);
        commander = new CommanderClient();
        virtualMachineBuilder = new VirtualMachineBuilder();
        openstackCloudProvider = new OpenstackCloudProvider(this.getOpenstackProperties());
    }
    
    CreateEasyCloudResourceTask(CommanderClient commander, VirtualMachineBuilder virtualMachineBuilder, OpenstackCloudProvider openstackCloudProvider, Object jsonObject, Project project) {
        super(project);
        this.project = project;
        this.commander = commander;        
        this.populateGlobalProperties(jsonObject);
        this.virtualMachineBuilder = virtualMachineBuilder;
        this.openstackCloudProvider = openstackCloudProvider;
    }
    
    CreateEasyCloudResourceTask(CommanderClient commander, Object jsonObject, Project project) {
        super(project);
        this.project = project;
        this.commander = commander;
        this.populateGlobalProperties(jsonObject);
        virtualMachineBuilder = new VirtualMachineBuilder();
        openstackCloudProvider = new OpenstackCloudProvider(this.getOpenstackProperties());
    }

    def createEasyCloudResource() {
        super.validate()
        LOGGER.info("Beginning provisioning of VM")
        Set<String> osNetworkNameSet, osSecurityGroupSet;
        List<VMMetadata> vmMetadatas = new ArrayList<VMMetadata>();
        Map<String, String> userMetadata = new HashMap<String, String>();
        osNetworkNameSet = new HashSet<String>();
        osSecurityGroupSet = new HashSet<String>();
        
        osNetworkNameSet.add(globalProperties.get(CreateEasyCloudResourceTask.Constants.OS_NETWORKNAME));
        
        globalProperties.get(CreateEasyCloudResourceTask.Constants.OS_SECURITY_GROUP_SET).split(',').each { eachSecGroup ->
            osSecurityGroupSet.add(eachSecGroup)
        }

        userMetadata.put("roleName", globalProperties.get(CreateEasyCloudResourceTask.Constants.ROLE_NAME));
        int count = Integer.parseInt(globalProperties.get(CreateEasyCloudResourceTask.Constants.NUMBER_OF_INSTANCES));

        vmMetadatas = virtualMachineBuilder.withProvider(openstackCloudProvider).withGroupName(globalProperties.get(CreateEasyCloudResourceTask.Constants.HOSTNAME))
                      .withHardwareProfileNameRegex(globalProperties.get(CreateEasyCloudResourceTask.Constants.OS_FLAVOR)).withOsImageRegex(globalProperties.get(CreateEasyCloudResourceTask.Constants.OS_IMAGENAME)).withNetworks(osNetworkNameSet)
                      .withSecurityGroups(osSecurityGroupSet).withKeypairName(globalProperties.get(CreateEasyCloudResourceTask.Constants.OS_KEYNAME)).withUserData(this.getUserDataString())
                      .withUserMetadata(userMetadata).buildVirtualMachine(count);
                      
        for (VMMetadata eachVMMetadata : vmMetadatas) {
            LOGGER.info(eachVMMetadata.toString())
        }
        this.postVMCreationSteps(vmMetadatas);        
    }
    
    private void postVMCreationSteps(List<VMMetadata> vmMetadatas) {
        successfullyRegisteredInstances = Collections.synchronizedList(new ArrayList<VMMetadata>());
        LOGGER.info("Post creation steps of VMs");         
        
        ExecutorService postVMCreationExecutor = Executors.newFixedThreadPool(VM_CUSTOMIZATION_THREADS);
        
        for (VMMetadata eachVMMetadata : vmMetadatas) {            
            Runnable worker = new PostVMCreationThread(globalProperties, successfullyRegisteredInstances, eachVMMetadata, commander);
            postVMCreationExecutor.execute(worker);
        }
        postVMCreationExecutor.shutdown();
        while(!postVMCreationExecutor.isTerminated());
        
        LOGGER.info("*******************************************************************");
        LOGGER.info("ALL VMS that were spun up on Openstack are as follows")
        for (VMMetadata eachVMMetadata : vmMetadatas) {
            LOGGER.info(eachVMMetadata.toString())
        }
//        def jsonAll = JsonOutput.toJson(vmMetadatas);
//        project.gapCloud.jsonAllInstances = jsonAll;
        
        LOGGER.info("*******************************************************************");
        LOGGER.info("ALL VMS that were spun up on Openstack and successfully registered with ETCD are as follows")
        for (VMMetadata eachVMMetadata : successfullyRegisteredInstances) {
            LOGGER.info(eachVMMetadata.toString())
        }
        LOGGER.info("*******************************************************************");
 //       def jsonRegistered = JsonOutput.toJson(successfullyRegisteredInstances);
//        project.gapCloud.jsonAllRegisteredInstances = jsonRegistered;
        
        if (globalProperties.get(CreateEasyCloudResourceTask.Constants.CREATE_EC_RESOURCE_FLAG) != null) {
            if ((globalProperties.get(CreateEasyCloudResourceTask.Constants.CREATE_EC_RESOURCE_FLAG)).equalsIgnoreCase("true")) {
                LOGGER.info("Setting up EC resources...")
                ExecutorService createECResourceExecutor = Executors.newFixedThreadPool(VM_CUSTOMIZATION_THREADS);
                for (VMMetadata eachVMMetadata : vmMetadatas) {
                    Runnable worker = new CreateECResourceThread(globalProperties, commander, eachVMMetadata);
                    createECResourceExecutor.execute(worker);
                }
                createECResourceExecutor.shutdown();
                while(!createECResourceExecutor.isTerminated());
            }
        }        
    }    
    
    private void populateGlobalProperties(Object jsonObject) {
        def tenantName, machineType, networkType;
        StringBuilder sb = new StringBuilder();
        LOGGER.info("Reading Json properties");
        globalProperties = new Properties();
        
        globalProperties.put(CreateEasyCloudResourceTask.Constants.START_TIME_IN_SECS, ((int)System.currentTimeMillis() / 1000));
        
        if (project.hasProperty('hostname')) {
            globalProperties.put(CreateEasyCloudResourceTask.Constants.HOSTNAME, project.hostname);
        } else {
            throw new InvalidPropertyAccessException("Hostname property not set in project. Please set project property 'hostname'");
        }
        
        if (project.hasProperty('tenant')) {
            tenantName = project.tenant;
            if (jsonObject[CreateEasyCloudResourceTask.Constants.JSON_OBJECT_TENANTS][tenantName] != null) {
                globalProperties.put(CreateEasyCloudResourceTask.Constants.JSON_TENANT, tenantName);
                globalProperties.put(CreateEasyCloudResourceTask.Constants.OS_USERNAME, jsonObject[CreateEasyCloudResourceTask.Constants.JSON_OBJECT_TENANTS][tenantName][CreateEasyCloudResourceTask.Constants.OS_USERNAME]);
                globalProperties.put(CreateEasyCloudResourceTask.Constants.OS_PASSWORD, jsonObject[CreateEasyCloudResourceTask.Constants.JSON_OBJECT_TENANTS][tenantName][CreateEasyCloudResourceTask.Constants.OS_PASSWORD]);
                globalProperties.put(CreateEasyCloudResourceTask.Constants.OS_ENDPOINT, jsonObject[CreateEasyCloudResourceTask.Constants.JSON_OBJECT_TENANTS][tenantName][CreateEasyCloudResourceTask.Constants.OS_ENDPOINT]);
                globalProperties.put(CreateEasyCloudResourceTask.Constants.OS_TENANT, jsonObject[CreateEasyCloudResourceTask.Constants.JSON_OBJECT_TENANTS][tenantName][CreateEasyCloudResourceTask.Constants.OS_TENANT]);
                globalProperties.put(CreateEasyCloudResourceTask.Constants.OS_KEYNAME, jsonObject[CreateEasyCloudResourceTask.Constants.JSON_OBJECT_TENANTS][tenantName][CreateEasyCloudResourceTask.Constants.OS_KEYNAME]);
                globalProperties.put(CreateEasyCloudResourceTask.Constants.OS_AVAILABILITY_ZONE, jsonObject[CreateEasyCloudResourceTask.Constants.JSON_OBJECT_TENANTS][tenantName][CreateEasyCloudResourceTask.Constants.OS_AVAILABILITY_ZONE]);
                
            } else {
                throw new InvalidPropertyAccessException("No such tenant " + tenantName + " exists in the cloud. Please check tenant name");
            }
        } else {
             throw new InvalidPropertyAccessException("Tenant property not set in project. Please set project property 'tenant'");
        }
        
        if (project.hasProperty('type')) {
            machineType = project.type;
            if (jsonObject[CreateEasyCloudResourceTask.Constants.JSON_OBJECT_MACHINES][machineType] != null) {
               globalProperties.put(CreateEasyCloudResourceTask.Constants.JSON_VM_TYPE, machineType);
               globalProperties.put(CreateEasyCloudResourceTask.Constants.DNATYPE, jsonObject[CreateEasyCloudResourceTask.Constants.JSON_OBJECT_MACHINES][machineType][CreateEasyCloudResourceTask.Constants.DNATYPE]);
               globalProperties.put(CreateEasyCloudResourceTask.Constants.OS_IMAGENAME, jsonObject[CreateEasyCloudResourceTask.Constants.JSON_OBJECT_MACHINES][machineType][CreateEasyCloudResourceTask.Constants.OS_IMAGENAME]);
               globalProperties.put(CreateEasyCloudResourceTask.Constants.PHONE_TIMEOUT, jsonObject[CreateEasyCloudResourceTask.Constants.JSON_OBJECT_MACHINES][machineType][CreateEasyCloudResourceTask.Constants.PHONE_TIMEOUT]);
            } else {
                throw new InvalidPropertyAccessException("No such machine type " + machineType + " exists. Please check type");
            }
         } else {            
             machineType = jsonObject[CreateEasyCloudResourceTask.Constants.JSON_OBJECT_TENANTS][tenantName][CreateEasyCloudResourceTask.Constants.JSON_OBJECT_DEFAULT_MACHINE];
             globalProperties.put(CreateEasyCloudResourceTask.Constants.JSON_VM_TYPE, machineType);
             globalProperties.put(CreateEasyCloudResourceTask.Constants.DNATYPE, jsonObject[CreateEasyCloudResourceTask.Constants.JSON_OBJECT_MACHINES][machineType][CreateEasyCloudResourceTask.Constants.DNATYPE]);
             globalProperties.put(CreateEasyCloudResourceTask.Constants.OS_IMAGENAME, jsonObject[CreateEasyCloudResourceTask.Constants.JSON_OBJECT_MACHINES][machineType][CreateEasyCloudResourceTask.Constants.OS_IMAGENAME]);
             globalProperties.put(CreateEasyCloudResourceTask.Constants.PHONE_TIMEOUT, jsonObject[CreateEasyCloudResourceTask.Constants.JSON_OBJECT_MACHINES][machineType][CreateEasyCloudResourceTask.Constants.PHONE_TIMEOUT]);                 
        }
        
        if (project.hasProperty('network')) {
            networkType = project.network;
            if (jsonObject[CreateEasyCloudResourceTask.Constants.JSON_OBJECT_NETWORKS][networkType] != null) {
              globalProperties.put(CreateEasyCloudResourceTask.Constants.JSON_NETWORK_TYPE, networkType);
              globalProperties.put(CreateEasyCloudResourceTask.Constants.OS_NETWORKNAME, jsonObject[CreateEasyCloudResourceTask.Constants.JSON_OBJECT_NETWORKS][networkType][CreateEasyCloudResourceTask.Constants.OS_NETWORKNAME]);
              globalProperties.put(CreateEasyCloudResourceTask.Constants.OS_NETWORK_ECZONE, jsonObject[CreateEasyCloudResourceTask.Constants.JSON_OBJECT_NETWORKS][networkType][CreateEasyCloudResourceTask.Constants.OS_NETWORK_ECZONE]);
            } else {
                throw new InvalidPropertyAccessException("No such Network " + networkType + " exists. Please check network");
            }
                    
         } else {             
             networkType = jsonObject[CreateEasyCloudResourceTask.Constants.JSON_OBJECT_TENANTS][tenantName][CreateEasyCloudResourceTask.Constants.JSON_OBJECT_DEFAULT_NETWORK];
             globalProperties.put(CreateEasyCloudResourceTask.Constants.JSON_NETWORK_TYPE, networkType);
             globalProperties.put(CreateEasyCloudResourceTask.Constants.OS_NETWORKNAME, jsonObject[CreateEasyCloudResourceTask.Constants.JSON_OBJECT_NETWORKS][networkType][CreateEasyCloudResourceTask.Constants.OS_NETWORKNAME]);
             globalProperties.put(CreateEasyCloudResourceTask.Constants.OS_NETWORK_ECZONE, jsonObject[CreateEasyCloudResourceTask.Constants.JSON_OBJECT_NETWORKS][networkType][CreateEasyCloudResourceTask.Constants.OS_NETWORK_ECZONE]);
        }
       
        if (project.hasProperty(CreateEasyCloudResourceTask.Constants.INFLUX_DB_URL)) {
            globalProperties.put(CreateEasyCloudResourceTask.Constants.INFLUX_DB_URL, project[CreateEasyCloudResourceTask.Constants.INFLUX_DB_URL]);            
            
        } else {
            globalProperties.put(CreateEasyCloudResourceTask.Constants.INFLUX_DB_URL, jsonObject[CreateEasyCloudResourceTask.Constants.INFLUX_DB_URL]);
        }
        
        if (project.hasProperty(CreateEasyCloudResourceTask.Constants.DNAURL)) {
            globalProperties.put(CreateEasyCloudResourceTask.Constants.DNAURL, project[CreateEasyCloudResourceTask.Constants.DNAURL]);
                        
        } else {
            globalProperties.put(CreateEasyCloudResourceTask.Constants.DNAURL, jsonObject[CreateEasyCloudResourceTask.Constants.DNAURL]);
        }
        
        if (project.hasProperty(CreateEasyCloudResourceTask.Constants.ETCD_HOSTNAME_URL)) {
            globalProperties.put(CreateEasyCloudResourceTask.Constants.ETCD_HOSTNAME_URL, project[CreateEasyCloudResourceTask.Constants.ETCD_HOSTNAME_URL]);
            
        } else {
            globalProperties.put(CreateEasyCloudResourceTask.Constants.ETCD_HOSTNAME_URL, jsonObject[CreateEasyCloudResourceTask.Constants.ETCD_HOSTNAME_URL]);            
        }
       
        if (project.hasProperty(CreateEasyCloudResourceTask.Constants.ETCD_KEY)) {
            globalProperties.put(CreateEasyCloudResourceTask.Constants.ETCD_KEY, project[CreateEasyCloudResourceTask.Constants.ETCD_KEY]);            
            
        } else {
            globalProperties.put(CreateEasyCloudResourceTask.Constants.ETCD_KEY, jsonObject[CreateEasyCloudResourceTask.Constants.ETCD_KEY]);
        }
        
        if (project.hasProperty(CreateEasyCloudResourceTask.Constants.OS_FLAVOR)) {
            globalProperties.put(CreateEasyCloudResourceTask.Constants.OS_FLAVOR, project[CreateEasyCloudResourceTask.Constants.OS_FLAVOR]);            
            
        } else {
            globalProperties.put(CreateEasyCloudResourceTask.Constants.OS_FLAVOR, jsonObject[CreateEasyCloudResourceTask.Constants.OS_FLAVOR]);
        }
        
        if (project.hasProperty(CreateEasyCloudResourceTask.Constants.NUMBER_OF_INSTANCES)) {
            globalProperties.put(CreateEasyCloudResourceTask.Constants.NUMBER_OF_INSTANCES, project[CreateEasyCloudResourceTask.Constants.NUMBER_OF_INSTANCES]);
            
        } else {
            globalProperties.put(CreateEasyCloudResourceTask.Constants.NUMBER_OF_INSTANCES, jsonObject[CreateEasyCloudResourceTask.Constants.NUMBER_OF_INSTANCES]);            
        }
       
        if (project.hasProperty(CreateEasyCloudResourceTask.Constants.ROLE_NAME)) {
            globalProperties.put(CreateEasyCloudResourceTask.Constants.ROLE_NAME, project[CreateEasyCloudResourceTask.Constants.ROLE_NAME]);            
        } else {
            globalProperties.put(CreateEasyCloudResourceTask.Constants.ROLE_NAME, "");
        }
        
        if (project.hasProperty(CreateEasyCloudResourceTask.Constants.OS_SECURITY_GROUP_SET)) {
            globalProperties.put(CreateEasyCloudResourceTask.Constants.OS_SECURITY_GROUP_SET, project[CreateEasyCloudResourceTask.Constants.OS_SECURITY_GROUP_SET].replace(" ", ""));            
            
        } else {
            jsonObject[CreateEasyCloudResourceTask.Constants.OS_SECURITY_GROUP_SET].each { eachSecGroup ->
                if (sb.length() == 0) {
                    sb.append(eachSecGroup);
                } else {
                    sb.append(",");
                    sb.append(eachSecGroup)
                }
            }
            globalProperties.put(CreateEasyCloudResourceTask.Constants.OS_SECURITY_GROUP_SET, sb.toString().replace(" ", ""));
        }   
        
        if (project.hasProperty(CreateEasyCloudResourceTask.Constants.CREATE_EC_RESOURCE_FLAG)) {
            globalProperties.put(CreateEasyCloudResourceTask.Constants.CREATE_EC_RESOURCE_FLAG, project[CreateEasyCloudResourceTask.Constants.CREATE_EC_RESOURCE_FLAG])
            if (project.hasProperty(CreateEasyCloudResourceTask.Constants.POOL_NAME)) {
                globalProperties.put(CreateEasyCloudResourceTask.Constants.POOL_NAME, project[CreateEasyCloudResourceTask.Constants.POOL_NAME])
            }
        } 
        
        if (project.hasProperty(CreateEasyCloudResourceTask.Constants.AUTO_PURGE)) {
            globalProperties.put(CreateEasyCloudResourceTask.Constants.AUTO_PURGE, (String)project[CreateEasyCloudResourceTask.Constants.AUTO_PURGE].toLowerCase());
        } else {
            globalProperties.put(CreateEasyCloudResourceTask.Constants.AUTO_PURGE, jsonObject[CreateEasyCloudResourceTask.Constants.AUTO_PURGE]);            
        }
        /*    for (Map.Entry<String, String> entry : globalProperties.entrySet()) {
         println (entry.getKey() + " ==> " + entry.getValue())
     }*/
    }

    private Properties getOpenstackProperties() {
        Properties properties = new Properties();
        properties.put("username", globalProperties.get(CreateEasyCloudResourceTask.Constants.OS_USERNAME));
        properties.put("password", globalProperties.get(CreateEasyCloudResourceTask.Constants.OS_PASSWORD));
        properties.put("endpoint", globalProperties.get(CreateEasyCloudResourceTask.Constants.OS_ENDPOINT));
        properties.put("tenant", globalProperties.get(CreateEasyCloudResourceTask.Constants.OS_TENANT));
        return properties;
    }

    private String getUserDataString() {
        String userData = null;
        String openstackUserDataUrl = globalProperties.get(CreateEasyCloudResourceTask.Constants.DNAURL) + "/" + globalProperties.get(CreateEasyCloudResourceTask.Constants.DNATYPE);
        def command = ['curl', '-s', '-k', openstackUserDataUrl]
        def process = command.execute()
        def exitCode = process.waitFor()
        def output  = process.in.text
        
        if (exitCode == 0) {
            LOGGER.info("Successfully read Userdata")
            userData = output
       //     LOGGER.debug("User data is: " + userData)
        } else {
            LOGGER.info("Could not read Usedata. Setting user data as null")
        }        
        return userData;
    }
    
    static class Constants {
        private Constants() {
           
        }
        public static final String INFLUX_DB_URL = "inFluxDBUrl";
        public static final String START_TIME_IN_SECS = "startTimeInSeconds";
        public static final String OS_USERNAME = "osUsername";
        public static final String OS_PASSWORD = "osPassword";
        public static final String OS_ENDPOINT = "osEndpoint";
        public static final String OS_TENANT = "osTenant";
        public static final String OS_AVAILABILITY_ZONE = "osAvailabilityZone";
        public static final String DNAURL = "DNAURL";
        public static final String DNATYPE = "DNAType";
        public static final String ETCD_HOSTNAME_URL = "etcdHostnameUrl";
        public static final String ETCD_KEY = "etcdKey";
        public static final String HOSTNAME = "hostname";
        public static final String OS_FLAVOR = "osFlavor";
        public static final String OS_IMAGENAME = "osImageName";
        public static final String OS_KEYNAME = "osKeyName";
        public static final String PHONE_TIMEOUT = "phoneTimeOut";
        public static final String ROLE_NAME = "roleName";
        public static final String NUMBER_OF_INSTANCES = "numberOfInstances";
        public static final String OS_NETWORKNAME = "osNetworkName";
        public static final String OS_NETWORK_ECZONE = "ecZone";
        public static final String OS_SECURITY_GROUP_SET = "osSecurityGroupSet"; 
        public static final String JSON_OBJECT_TENANTS = "tenants"; 
        public static final String JSON_OBJECT_NETWORKS = "networks"; 
        public static final String JSON_OBJECT_MACHINES = "machines"; 
        public static final String JSON_OBJECT_DEFAULT_MACHINE = "defaultMachine";
        public static final String JSON_OBJECT_DEFAULT_NETWORK = "defaultNetwork";   
        public static final String CREATE_EC_RESOURCE_FLAG = "createResource";  
        public static final String POOL_NAME = "pool";  
        public static final String JSON_VM_TYPE = "vmType"; 
        public static final String JSON_TENANT = "tenant"; 
        public static final String JSON_NETWORK_TYPE = "networkType"; 
        public static final String AUTO_PURGE = "autoPurge";
    }
}
