package com.gap.gradle.utils

import org.apache.commons.logging.LogFactory

import com.gap.gradle.exceptions.RetryTimeOutException

public class RetryCommand{

    public static void executeWithRetry(timeToWaitInMinutes, intervalInMinutes, closure){
		def logger = LogFactory.getLog(RetryCommand)
        def timeoutMillis = timeToWaitInMinutes * 60000
        def start = System.currentTimeMillis()
        def end  = start + timeoutMillis
		def count=0
        while (!closure()) {
			logger.info("Trying " + ++count)
			sleep((intervalInMinutes * 60000).toLong())
            if (System.currentTimeMillis() > end) {
                def message = "Timed out after ${timeoutMillis} ms waiting."
                throw new RetryTimeOutException(message)
            }
        }
    }
}