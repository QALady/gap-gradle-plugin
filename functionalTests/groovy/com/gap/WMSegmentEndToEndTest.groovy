package com.gap

import com.gap.util.ECClient;
import com.gap.util.Util;
import org.junit.Test
import static junit.framework.Assert.assertEquals
import static junit.framework.Assert.assertTrue
import java.lang.Thread

public class WMSegmentEndToEndTest {
    def ec = new ECClient()

    @Test
    public void shouldSuccessfullyExecuteTheFullPipeline() {
        def upstreamJobId = ec.runProcedureSync("Watchmen Test Segments:Component Segment")
        assertEquals("success", ec.getJobStatus(upstreamJobId).outcome.toString())

        String downstreamSVNJobID = getDownstreamJobId(upstreamJobId, "Watchmen Test Segments:ISO Segment")
        assertTrue(downstreamSVNJobID.length() > 0)

        waitForDownstreamJobToComplete( downstreamSVNJobID)
        assertEquals("success", ec.getJobStatus(downstreamSVNJobID).outcome.toString())

        String downstreamGitJobID = getDownstreamJobId(upstreamJobId, "Watchmen Test Segments:ISO Segment Git")
        assertTrue(downstreamGitJobID.length() > 0)

        waitForDownstreamJobToComplete( downstreamGitJobID)
        assertEquals("success", ec.getJobStatus(downstreamGitJobID).outcome.toString())
    }

    private void waitForDownstreamJobToComplete(isoJobID) {
        Util.executeWithRetry(10, 2, {
            ec.getJobStatus(isoJobID).status == 'completed'
        })
    }


    private String getDownstreamJobId(componetJobId, ecProcName) {
        def isoJobURL = ""
        Util.executeWithRetry(3, 0.5,
                {
                    try {
                        isoJobURL = ec.getProperty("report-urls/${ecProcName}", componetJobId)
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
