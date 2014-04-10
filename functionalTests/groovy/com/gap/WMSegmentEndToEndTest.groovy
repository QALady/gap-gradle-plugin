package com.gap

import com.gap.util.ECClient;
import com.gap.util.Util;
import org.junit.Test
import static junit.framework.Assert.assertEquals
import static junit.framework.Assert.assertTrue

public class WMSegmentEndToEndTest {
    def ec = new ECClient()

    @Test
    public void shouldSuccessfullyExecuteTheFullPipeline() {
        def upstreamJobId = ec.runProcedureSync("Watchmen Test Segments:Component Segment")
        assertEquals("success", ec.getJobStatus(upstreamJobId).outcome.toString())

        String downstreamJobID = getDownstreamJobId(upstreamJobId)
        assertTrue( downstreamJobID.length() > 0)

        waitForDownstreamJobToComplete( downstreamJobID)
        assertEquals("success", ec.getJobStatus( downstreamJobID).outcome.toString())
    }

    private void waitForDownstreamJobToComplete(isoJobID) {
        Util.executeWithRetry(10, 2, {
            ec.getJobStatus(isoJobID).status == 'completed'
        })
    }

    private String getDownstreamJobId(componetJobId) {
        def isoJobURL = ""
        Util.executeWithRetry(3, 0.5,
                {
                    try {
                        isoJobURL = ec.getProperty("report-urls/Watchmen Test Segments:ISO Segment", componetJobId)
                        return true;
                    } catch (Exception) {
                        return false;
                    }
                }
        )
        // /commander/link/jobDetails/jobs/2971097
        def extractJobIdRegexPattern = /[0-9]+$/
        (isoJobURL =~ extractJobIdRegexPattern)[0]
    }
}
