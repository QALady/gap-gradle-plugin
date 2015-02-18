package com.gap.gradle.tasks
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class SpawnBackgroundProcessTask extends DefaultTask {

    String command
    String processReadyText
    String directory

    SpawnBackgroundProcessTask() {
        directory = '.'
    }

    @TaskAction
    def exec() {
        if (!(command)) {
            throw new GradleException("Please define `command` to execute")
        }

        Process process = invokeCommand()
        waitFor()
    }

    private Process invokeCommand() {
         new ProcessBuilder(command.split())
                .redirectErrorStream(true)
                .directory(new File(directory))
                .start()
    } 
    
    private waitFor() {

      //wait timeout 60 secs and retry time 5 secs
        int max_retries = 12
        int retry_interval = 5
        int number_of_processes =  0
        int retry_count = 1
        def cmd = "ps -ef | grep '${command}' | grep -v grep |wc | awk '{print \$1}'"
        println " waiting for process to exist ${command}"
        while (number_of_processes != 1){   
               
            number_of_processes = ["bash","-c", cmd].execute(null,new File(directory)).text.toInteger()
            println "number of processes ....." + number_of_processes
            if (number_of_processes == 1) {
                break;
            }
            if (retry_count == max_retries) {
                throw new MaxRetriesExceededException("Process Timedout: ${command} didnt start in ${max_retries * retry_interval} secs");
            }
            retry_count += 1
            sleep  retry_interval * 1000
        }

    }
    public class MaxRetriesExceededException extends RuntimeException {
        public MaxRetriesExceededException(String message) {
            super(message)
        }
    }
}
