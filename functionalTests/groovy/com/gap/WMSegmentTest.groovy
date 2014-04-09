package com.gap

import com.gap.util.ECClient;
import org.junit.Test
import static junit.framework.Assert.assertEquals

public class WMSegmentTest {
    def ec = new ECClient()

    @Test
    public void sampleTest(){
        assertEquals(true, true)
    }

    @Test
    public void shouldTriggerComponentSegmentSuccessfully() {

        def jobId = ec.runProcedure("Watchmen Test Segments:Component Segment")
        
        waitFor(10, {
            ec.getJobStatus(jobId).status == 'completed'
        })
    }

    private waitFor(timeToWaitInMinutes, closure){
        def start = System.currentTimeMillis()
        def end  = start + timeoutMillis

        while (!closure()) {
            sleep(10000)
            if (System.currentTimeMillis() > end) {
                def message = "Timed out after ${timeoutMillis} ms waiting for job to finish"
                throw new Exception(message)
            }
        }
    }
}