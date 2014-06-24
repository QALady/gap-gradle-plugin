package com.gap.util

public class Util{

    public static void executeWithRetry(timeToWaitInMinutes, intervalInMinutes, closure){
        def timeoutMillis = timeToWaitInMinutes * 60000
        def start = System.currentTimeMillis()
        def end  = start + timeoutMillis

        while (!closure()) {
            sleep((intervalInMinutes * 60000).toLong())
            if (System.currentTimeMillis() > end) {
                def message = "Timed out after ${timeoutMillis} ms waiting."
                throw new Exception(message)
            }
        }
    }
}