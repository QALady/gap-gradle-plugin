package com.gap.gradle.threads

import com.gap.cloud.VMMetadata;
import com.gap.pipeline.ec.CommanderClient;

import java.text.SimpleDateFormat

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory;

import static com.gap.gradle.tasks.CreateEasyCloudResourceTask.Constants.*;

class CreateECResourceThread implements Runnable {
    
    private static final Log LOGGER = LogFactory.getLog(com.gap.gradle.threads.CreateECResourceThread);
    private Properties properties;
    private CommanderClient commander
    private VMMetadata vmMetadata;
    
    CreateECResourceThread(Properties properties, CommanderClient commander, VMMetadata vmMetadata) {
        this.properties = properties;
        this.commander = commander;
        this.vmMetadata = vmMetadata;
    }

    @Override
    public void run() {
        Map<String, String> createArgsRef = new HashMap<String, String>();
        Map<String, String> ecPropertyMap = new HashMap<String, String>();
        
        String poolName = "";
        String expirationTime = this.getExpirationDateProperty();
        String vmType = properties.getProperty(JSON_VM_TYPE);
        String hostname = vmMetadata.getHostname();
        
        if (properties.get(POOL_NAME) != null) {
            poolName = properties.getProperty(POOL_NAME)
        } 
        
        String tenant = properties.getProperty(JSON_TENANT);
        String ip = this.getIPString(vmMetadata);
        String id = vmMetadata.getProviderId();
        String image = properties.getProperty(OS_IMAGENAME);
        String ecZone = properties.getProperty(OS_NETWORK_ECZONE);
        
        createArgsRef.put("hostName", ip);
        createArgsRef.put("zoneName", ecZone);
        createArgsRef.put("pools", poolName);
        createArgsRef.put("block", "1");
        
        ecPropertyMap.put("/resources/" + hostname + "/open-stack-id", id);
        ecPropertyMap.put("/resources/" + hostname + "/open-stack-hostname", hostname);
        ecPropertyMap.put("/resources/" + hostname + "/ip", ip);
        ecPropertyMap.put("/resources/" + hostname + "/tenant", tenant);
        ecPropertyMap.put("/myJob/osResources/" + hostname + "/ready", "true");
        ecPropertyMap.put("/resources/" + hostname + "/networkType", properties.getProperty(JSON_NETWORK_TYPE));
        ecPropertyMap.put("/resources/" + hostname + "/expirationTime", expirationTime);
        ecPropertyMap.put("/resources/" + hostname + "/image", image);
        ecPropertyMap.put("/resources/" + hostname + "/autoPurge", properties.getProperty(AUTO_PURGE));
        ecPropertyMap.put("/resources/" + hostname + "/createdByJobId", commander.getJobId());
        
        createArgsRef.put("workspaceName", "shared build");
        
        if (vmType.contains("tdev")) {
            vmType = "tdev";
        }
        
        switch(vmType) {
            case "chef" : //do Nothing
                          break;
            
            case "win": createArgsRef.put("workspaceName", "shared build noauth");
                        ecPropertyMap.put("/resources/" + hostname + "/ramdiskPath", "C:\\svn");
                        ecPropertyMap.put("/resources/" + hostname + "/currentState", "");
                        break;
            
            case "windebug": createArgsRef.put("workspaceName", "shared build noauth");
                             ecPropertyMap.put("/resources/" + hostname + "/ramdiskPath", "C:\\svn");
                             ecPropertyMap.put("/resources/" + hostname + "/currentState", "");
                             break;
                             
            case "build": createArgsRef.put("stepLimit", "6");
                          ecPropertyMap.put("/resources/" + hostname + "/ramdiskPath", "/home/eggbuild/shm");
                          break;
                          
            case "tdev": createArgsRef.put("workspaceName", "default");
                         break;            
        } 
        LOGGER.info("Making VM " + hostname + " an EC Resource")       
        commander.createResource(hostname, createArgsRef);
        for (Map.Entry<String, String> entry : ecPropertyMap.entrySet()) {
            commander.setECProperty(entry.getKey(), entry.getValue());
        }
    }
    
    private String getExpirationDateProperty() {
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd'T'hh:mm:ss.'0000Z'");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); 
        c.add(Calendar.HOUR, 6); 
        String output = sdf.format(c.getTime());
        LOGGER.info(output);
        return output;
    }
    
    private String getIPString(VMMetadata vmMetadata) {
        StringBuilder sb = new StringBuilder();
        vmMetadata.getPublicAddresses().each {eachIP ->
            if (sb.length() == 0)
            {sb.append(eachIP)}
            else {
                sb.append(",")
                sb.append(eachIP)}}
        vmMetadata.getPrivateAddresses().each {eachIP ->
            if (sb.length() == 0)
            {sb.append(eachIP)}
            else {
                sb.append(",")
                sb.append(eachIP)}}
        return sb.toString();
    }
}
