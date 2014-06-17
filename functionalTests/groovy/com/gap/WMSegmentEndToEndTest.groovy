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
}
