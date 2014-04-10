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

        waitFor(10, 2, {
            ec.getJobStatus(jobId).status == 'completed'
        })

        sleep(10000) //Sleeping for 10 seconds, so downstream segment will be triggered

        def isoJobURL = ""
        waitFor(3, 0.5,
                {
                    try{
                        isoJobURL = ec.getProperty("report-urls/Watchmen Test Segments:ISO Segment", jobId)
                        return true;
                    } catch (Exception){
                        return false;
                    }
                }
        )

        def isoJobID = isoJobURL.split("/")[5]
        println("JOB ID: ${isoJobID}")

        waitFor(10, 2, {
            ec.getJobStatus(isoJobID).status == 'completed'
        })
    }

    private waitFor(timeToWaitInMinutes, intervalInSeconds, closure){
        def timeoutMillis = timeToWaitInMinutes * 60000
        def start = System.currentTimeMillis()
        def end  = start + timeoutMillis

        while (!closure()) {
            sleep(intervalInSeconds * 60000)
            if (System.currentTimeMillis() > end) {
                def message = "Timed out after ${timeoutMillis} ms waiting."
                throw new Exception(message)
            }
        }
    }
}
