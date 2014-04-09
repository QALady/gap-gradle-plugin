package com.gap

import com.gap.util.ECClient;
import org.junit.Test
import static junit.framework.Assert.assertEquals

public class WMSegmentTest {


    @Test
    public void sampleTest(){
        assertEquals(true, true)
    }

    @Test
    public void shouldTriggerComponentSegmentSuccessfully(){
        def ec = new ECClient()
        ec.runProcedure("Watchmen Test Segments:Component Segment")
    }



}