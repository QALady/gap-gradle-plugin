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
import com.gap.pipeline.tasks.annotations.RequiredParameters;
import com.google.common.base.Function
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import groovy.json.JsonSlurper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

import static com.gap.gradle.tasks.CreateEasyCloudResourceTask.Constants.*

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
        
        osNetworkNameSet.add(globalProperties.get(OS_NETWORKNAME));
        
        globalProperties.get(OS_SECURITY_GROUP_SET).split(',').each { eachSecGroup ->
            osSecurityGroupSet.add(eachSecGroup)
        }

        userMetadata.put("roleName", globalProperties.get(ROLE_NAME));
        int count = Integer.parseInt(globalProperties.get(NUMBER_OF_INSTANCES));

        vmMetadatas = virtualMachineBuilder.withProvider(openstackCloudProvider).withGroupName(globalProperties.get(HOSTNAME))
                      .withHardwareProfileNameRegex(globalProperties.get(OS_FLAVOR)).withOsImageRegex(globalProperties.get(OS_IMAGENAME)).withNetworks(osNetworkNameSet)
                      .withSecurityGroups(osSecurityGroupSet).withKeypairName(globalProperties.get(OS_KEYNAME)).withUserData(this.getUserDataString())
                      .withUserMetadata(userMetadata).buildVirtualMachine(count);
                      
        for (VMMetadata eachVMMetadata : vmMetadatas) {
            LOGGER.info(eachVMMetadata.toString())
        }
        this.postVMCreationSteps(vmMetadatas);        
    }
    
    private void postVMCreationSteps(List<VMMetadata> vmMetadatas) {
        LOGGER.info("Post creation steps of VMs");         
        
        ExecutorService postVMCreationExecutor = Executors.newFixedThreadPool(VM_CUSTOMIZATION_THREADS);
        List<Future<Optional<VMMetadata>>> futureExecutions = new ArrayList<Future<Optional<VMMetadata>>>();
        
        for (VMMetadata eachVMMetadata : vmMetadatas) {            
            Future<Optional<VMMetadata>> futureExecution = postVMCreationExecutor.submit(new PostVMCreationThread(globalProperties, eachVMMetadata, commander));
            futureExecutions.add(futureExecution);
        }
    
        successfullyRegisteredInstances = Lists.newArrayList(
            Iterables.filter(
                Lists.transform(futureExecutions, new Function<Future<Optional<VMMetadata>>, VMMetadata>() {
                    @Override
                    public VMMetadata apply(Future<Optional<VMMetadata>> future) {
                        if (future.get().isPresent()) {
                            return future.get().get();
                        } 
                    }
                }), Predicates.notNull()));
        
        LOGGER.info("*******************************************************************");
        LOGGER.info("ALL VMS that were spun up on Openstack are as follows")
        for (VMMetadata eachVMMetadata : vmMetadatas) {
            LOGGER.info(eachVMMetadata.toString())
        }       
        LOGGER.info("*******************************************************************");
        project.gapCloud.allInstances = vmMetadatas;
        
        LOGGER.info("ALL VMS that were spun up on Openstack and successfully registered with ETCD are as follows")
        for (VMMetadata eachVMMetadata : successfullyRegisteredInstances) {
            LOGGER.info(eachVMMetadata.toString())
        }
        LOGGER.info("*******************************************************************");
        project.gapCloud.allRegisteredInstances = successfullyRegisteredInstances;
        
        if (globalProperties.get(CREATE_EC_RESOURCE_FLAG) != null) {
            if ((globalProperties.get(CREATE_EC_RESOURCE_FLAG)).equalsIgnoreCase("true")) {
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
        
        globalProperties.put(START_TIME_IN_SECS, ((int)System.currentTimeMillis() / 1000));
        
        if (project.hasProperty('hostname')) {
            globalProperties.put(HOSTNAME, project.hostname);
        } else {
            throw new InvalidPropertyAccessException("Hostname property not set in project. Please set project property 'hostname'");
        }
        
        if (project.hasProperty('tenant')) {
            tenantName = project.tenant;
            if (jsonObject[JSON_OBJECT_TENANTS][tenantName] != null) {
                globalProperties.put(JSON_TENANT, tenantName);
                globalProperties.put(OS_USERNAME, jsonObject[JSON_OBJECT_TENANTS][tenantName][OS_USERNAME]);
                globalProperties.put(OS_PASSWORD, jsonObject[JSON_OBJECT_TENANTS][tenantName][OS_PASSWORD]);
                globalProperties.put(OS_ENDPOINT, jsonObject[JSON_OBJECT_TENANTS][tenantName][OS_ENDPOINT]);
                globalProperties.put(OS_TENANT, jsonObject[JSON_OBJECT_TENANTS][tenantName][OS_TENANT]);
                globalProperties.put(OS_KEYNAME, jsonObject[JSON_OBJECT_TENANTS][tenantName][OS_KEYNAME]);
                globalProperties.put(OS_AVAILABILITY_ZONE, jsonObject[JSON_OBJECT_TENANTS][tenantName][OS_AVAILABILITY_ZONE]);
                
            } else {
                throw new InvalidPropertyAccessException("No such tenant " + tenantName + " exists in the cloud. Please check tenant name");
            }
        } else {
             throw new InvalidPropertyAccessException("Tenant property not set in project. Please set project property 'tenant'");
        }
        
        if (project.hasProperty('type')) {
            machineType = project.type;
            if (jsonObject[JSON_OBJECT_MACHINES][machineType] != null) {
               globalProperties.put(JSON_VM_TYPE, machineType);
               globalProperties.put(DNATYPE, jsonObject[JSON_OBJECT_MACHINES][machineType][DNATYPE]);
               globalProperties.put(OS_IMAGENAME, jsonObject[JSON_OBJECT_MACHINES][machineType][OS_IMAGENAME]);
               globalProperties.put(PHONE_TIMEOUT, jsonObject[JSON_OBJECT_MACHINES][machineType][PHONE_TIMEOUT]);
            } else {
                throw new InvalidPropertyAccessException("No such machine type " + machineType + " exists. Please check type");
            }
         } else {            
             machineType = jsonObject[JSON_OBJECT_TENANTS][tenantName][JSON_OBJECT_DEFAULT_MACHINE];
             globalProperties.put(JSON_VM_TYPE, machineType);
             globalProperties.put(DNATYPE, jsonObject[JSON_OBJECT_MACHINES][machineType][DNATYPE]);
             globalProperties.put(OS_IMAGENAME, jsonObject[JSON_OBJECT_MACHINES][machineType][OS_IMAGENAME]);
             globalProperties.put(PHONE_TIMEOUT, jsonObject[JSON_OBJECT_MACHINES][machineType][PHONE_TIMEOUT]);
        }
        
        if (project.hasProperty('network')) {
            networkType = project.network;
            if (jsonObject[JSON_OBJECT_NETWORKS][networkType] != null) {
              globalProperties.put(JSON_NETWORK_TYPE, networkType);
              globalProperties.put(OS_NETWORKNAME, jsonObject[JSON_OBJECT_NETWORKS][networkType][OS_NETWORKNAME]);
              globalProperties.put(OS_NETWORK_ECZONE, jsonObject[JSON_OBJECT_NETWORKS][networkType][OS_NETWORK_ECZONE]);
            } else {
                throw new InvalidPropertyAccessException("No such Network " + networkType + " exists. Please check network");
            }
                    
         } else {             
             networkType = jsonObject[JSON_OBJECT_TENANTS][tenantName][JSON_OBJECT_DEFAULT_NETWORK];
             globalProperties.put(JSON_NETWORK_TYPE, networkType);
             globalProperties.put(OS_NETWORKNAME, jsonObject[JSON_OBJECT_NETWORKS][networkType][OS_NETWORKNAME]);
             globalProperties.put(OS_NETWORK_ECZONE, jsonObject[JSON_OBJECT_NETWORKS][networkType][OS_NETWORK_ECZONE]);
        }
       
        if (project.hasProperty(INFLUX_DB_URL)) {
            globalProperties.put(INFLUX_DB_URL, project[INFLUX_DB_URL]);
            
        } else {
            globalProperties.put(INFLUX_DB_URL, jsonObject[INFLUX_DB_URL]);
        }
        
        if (project.hasProperty(DNAURL)) {
            globalProperties.put(DNAURL, project[DNAURL]);
                        
        } else {
            globalProperties.put(DNAURL, jsonObject[DNAURL]);
        }
        
        if (project.hasProperty(ETCD_HOSTNAME_URL)) {
            globalProperties.put(ETCD_HOSTNAME_URL, project[ETCD_HOSTNAME_URL]);
            
        } else {
            globalProperties.put(ETCD_HOSTNAME_URL, jsonObject[ETCD_HOSTNAME_URL]);
        }
       
        if (project.hasProperty(ETCD_KEY)) {
            globalProperties.put(ETCD_KEY, project[ETCD_KEY]);
            
        } else {
            globalProperties.put(ETCD_KEY, jsonObject[ETCD_KEY]);
        }
        
        if (project.hasProperty(OS_FLAVOR)) {
            globalProperties.put(OS_FLAVOR, project[OS_FLAVOR]);
            
        } else {
            globalProperties.put(OS_FLAVOR, jsonObject[OS_FLAVOR]);
        }
        
        if (project.hasProperty(NUMBER_OF_INSTANCES)) {
            globalProperties.put(NUMBER_OF_INSTANCES, project[NUMBER_OF_INSTANCES]);
            
        } else {
            globalProperties.put(NUMBER_OF_INSTANCES, jsonObject[NUMBER_OF_INSTANCES]);
        }
       
        if (project.hasProperty(ROLE_NAME)) {
            globalProperties.put(ROLE_NAME, project[ROLE_NAME]);
        } else {
            globalProperties.put(ROLE_NAME, "");
        }
        
        if (project.hasProperty(OS_SECURITY_GROUP_SET)) {
            globalProperties.put(OS_SECURITY_GROUP_SET, project[OS_SECURITY_GROUP_SET].replace(" ", ""));
            
        } else {
            jsonObject[OS_SECURITY_GROUP_SET].each { eachSecGroup ->
                if (sb.length() == 0) {
                    sb.append(eachSecGroup);
                } else {
                    sb.append(",");
                    sb.append(eachSecGroup)
                }
            }
            globalProperties.put(OS_SECURITY_GROUP_SET, sb.toString().replace(" ", ""));
        }   
        
        if (project.hasProperty(CREATE_EC_RESOURCE_FLAG)) {
            globalProperties.put(CREATE_EC_RESOURCE_FLAG, project[CREATE_EC_RESOURCE_FLAG])
            if (project.hasProperty(POOL_NAME)) {
                globalProperties.put(POOL_NAME, project[POOL_NAME])
            }
        } 
        
        if (project.hasProperty(AUTO_PURGE)) {
            globalProperties.put(AUTO_PURGE, (String)project[AUTO_PURGE].toLowerCase());
        } else {
            globalProperties.put(AUTO_PURGE, jsonObject[AUTO_PURGE]);
        }
        /*    for (Map.Entry<String, String> entry : globalProperties.entrySet()) {
         println (entry.getKey() + " ==> " + entry.getValue())
     }*/
    }

    private Properties getOpenstackProperties() {
        Properties properties = new Properties();
        properties.put("username", globalProperties.get(OS_USERNAME));
        properties.put("password", globalProperties.get(OS_PASSWORD));
        properties.put("endpoint", globalProperties.get(OS_ENDPOINT));
        properties.put("tenant", globalProperties.get(OS_TENANT));
        return properties;
    }

    private String getUserDataString() {
        String userData = null;
        String openstackUserDataUrl = globalProperties.get(DNAURL) + "/" + globalProperties.get(DNATYPE);
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
