package com.gap
import static junit.framework.Assert.assertEquals
import static junit.framework.Assert.assertTrue

import com.gap.util.ECClient
import com.gap.util.Util
import org.junit.Test

public class WMSegmentEndToEndTest {
    ECClient ec = new ECClient()

    @Test
    public void shouldSuccessfullyExecuteTheFullPipeline() {

        def pluginVersionUnderTest = ec.isRunningInPipeline()? ec.getECProperty('pluginVersion'):'+'

        def upstreamJobId = ec.runProcedureSync("Watchmen Test Segments:Component Segment", ['gapGradlePluginVersion': pluginVersionUnderTest])
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
        Util.executeWithRetry(100, 2, {
            ec.getJobStatus(isoJobID).status == 'completed'
        })
    }


    private String getDownstreamJobId(componetJobId, ecProcName) {
        def isoJobURL = ""
        Util.executeWithRetry(30, 0.5,
                {
                    try {
                        isoJobURL = ec.getECProperty("report-urls/${ecProcName}", componetJobId)
                        return true;
                    } catch (Exception) {
                        return false;
                    }
                }
        )

        
     def extractJobIdRegexPattern = /[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/

        (isoJobURL =~ extractJobIdRegexPattern)[0]
    }
}
