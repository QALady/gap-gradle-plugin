package com.gap.gradle.threads

import com.gap.cloud.VMMetadata;
import com.gap.gradle.utils.ShellCommandException;
import com.gap.pipeline.ec.CommanderClient;

import groovyx.net.http.HTTPBuilder;

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory;

import static com.gap.gradle.tasks.CreateEasyCloudResourceTask.Constants.*;
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

class PostVMCreationThread implements Runnable {
    
    private static final Log LOGGER = LogFactory.getLog(com.gap.gradle.threads.PostVMCreationThread);
    private List<VMMetadata> successfullyRegisteredInstances;
    private VMMetadata vmMetadata;
    private Properties properties;
    private CommanderClient commander;
    private String resultState;
    private String phaseState;
    
    public PostVMCreationThread(Properties properties, List<VMMetadata> successfullyRegisteredInstances, VMMetadata vmMetadata, CommanderClient commander) {
        this.properties = properties;
        this.successfullyRegisteredInstances = successfullyRegisteredInstances;
        this.vmMetadata = vmMetadata;
        this.commander = commander;
    }

    @Override
    public void run() {
        boolean phoneHomeStatus = false;
        def output;
        
        String etcdURL = properties.get(ETCD_HOSTNAME_URL) + "/" + properties.get(ETCD_KEY) + "/" + vmMetadata.getProviderId() + "?wait=true";
        String phoneHomeTimeout = properties.getProperty(PHONE_TIMEOUT);
        Integer timeout = Integer.parseInt(phoneHomeTimeout) * 1000;
        LOGGER.info("Waiting for Spun up VMs to register with etcd. Time to wait is " + phoneHomeTimeout + " seconds");
        LOGGER.info("etcd string is " + etcdURL);
        HTTPBuilder builder = new HTTPBuilder(etcdURL);
        builder.getClient().getParams().setParameter("http.socket.timeout", timeout);
        
        try {
            builder.request(GET, TEXT) {
                response.success = { resp, reader ->
                    output = reader.text
                    phoneHomeStatus = true;                 
                    LOGGER.info("Status code is " + resp.statusLine.statusCode);        
                }
            }
            
        } catch (SocketTimeoutException e) {
            LOGGER.info("ETCD registration did not occur within Max time");
        } catch (ConnectException e) {
            LOGGER.info("ETCD registration Connection refused");            
        } finally {
            builder.shutdown();
        }
        
        if (phoneHomeStatus) {
            LOGGER.info("VM " + vmMetadata.getHostname() + " successfully registered with etcd with output: " + output);
            
            LOGGER.info("Setting EC resource properties for " + vmMetadata.getHostname())
            String ip = this.getIPString(vmMetadata);
            try {
                commander.setECProperty("/myJob/osResources/" + vmMetadata.getHostname() + "/id", vmMetadata.getProviderId());
                commander.setECProperty("/myJob/osResources/" + vmMetadata.getHostname() + "/ip", ip);
                commander.setECProperty("/myJob/osResources/" + vmMetadata.getHostname() + "/image", properties.get(OS_IMAGENAME));
            } catch (ShellCommandException e) {
                LOGGER.info("Could not set EC Property.....Continuing " + e.getMessage());
            } catch (IOException e) {
                LOGGER.info("Could not set EC Property.....Continuing " + e.getMessage());
            }         
            phaseState = "exit_sucess";
            resultState = "success";
            successfullyRegisteredInstances.add(vmMetadata);
        } else {
            LOGGER.info("VM " + vmMetadata.getHostname() + " was NOT successfully registered with etcd")
            phaseState = "handle_phone_home_timeout"
            resultState = "failure";
        }
                
        /* Write Data to Influx DB */
        LOGGER.info("Writing data to influx db for " + vmMetadata.getHostname())
        this.writeDataToInfluxDB()
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
    
    private void writeDataToInfluxDB() {
        
        def ec_job_id = "";
        def ec_job_step_id = "";
        def ec_job_name = "";
        def duration = (int)(System.currentTimeMillis() / 1000) - (properties.get(START_TIME_IN_SECS));
        def result = resultState;
        def phase = phaseState;
        def openstack_tenant = properties.get(OS_TENANT);
        def openstack_network_uuid = properties.get(OS_NETWORKNAME);
        def openstack_image_name = properties.get(OS_IMAGENAME);
        def openstack_user_data_type = properties.get(DNATYPE);
        def id = vmMetadata.getProviderId();
        def host_id = vmMetadata.getLocation().getId();
        def instance_hostname = vmMetadata.getHostname();        
        def influxDBUrl = properties.get(INFLUX_DB_URL);
        try {
            ec_job_id = commander.getJobId();
        } catch (ShellCommandException e){
            LOGGER.info("Could not get EC JobId.....Continuing " + e.getMessage());
        } catch (IOException e) {
            LOGGER.info("Could not get EC JobId.....Continuing " + e.getMessage());
        } 
        try {
            ec_job_step_id = commander.getECProperty("/myJobStep/jobStepId");
        } catch (ShellCommandException e){
            LOGGER.info("Could not get EC Job Step Id.....Continuing " + e.getMessage());
        } catch (IOException e) {
            LOGGER.info("Could not get EC Job Step Id.....Continuing " + e.getMessage());
        } 
        try {
            ec_job_name = commander.getECProperty("/myJob/jobName");
        } catch (ShellCommandException e){
            LOGGER.info("Could not get EC Job Name.....Continuing " + e.getMessage());
        } catch (IOException e) {
            LOGGER.info("Could not get EC Job Name.....Continuing " + e.getMessage());
        } 
        
        def jsonData = """[
    {
        "columns": [
            "result",
            "ec_job_id",
            "ec_job_name",
            "ec_job_step_id",
            "tenant",
            "network",
            "image",
            "dna",
            "phase",
            "duration",
            "instance_id",
            "instance_name",
            "host_id"
        ],
        "name": "call_nova",
        "points": [
            [
                "${result}",
                "${ec_job_id}",
                "${ec_job_name}",
                "${ec_job_step_id}",
                "${openstack_tenant}",
                "${openstack_network_uuid}",
                "${openstack_image_name}",
                "${openstack_user_data_type}",
                "${phase}",
                ${duration},
                "${id}",
                "${instance_hostname}",
                "${host_id}"
            ]
        ]
    }
]"""
        def command = ['curl', '-m', '5', '-X', 'POST', '-d', jsonData, influxDBUrl]
        LOGGER.info("Influx DB curl URL is " + command.toString());
        def proc = command.execute()
        def exitCode = proc.waitFor()
        
        if (exitCode == 0) {
            LOGGER.info("Successfully written data to influx DB for " + vmMetadata.getHostname());
        } else {
            LOGGER.info("Could not write data to influx DB for " + vmMetadata.getHostname());
        }        
    }
}
