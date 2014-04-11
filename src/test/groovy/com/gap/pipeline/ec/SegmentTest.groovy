package com.gap.pipeline.ec
import static org.hamcrest.CoreMatchers.is
import static org.junit.Assert.assertThat

import org.junit.Test

class SegmentTest {


    @Test
    public void toString_shouldReturnSegmentIDInEcFormat(){
        def segment = new Segment("My Project", "My Procedure")

        assertThat(segment.toString(), is("My Project:My Procedure"))
    }

    @Test
    public void fromString_shouldCreateSegmentFromECFormat(){
        def segment = Segment.fromString("Some Project:Some Procedure")

        assertThat(segment.projectName, is('Some Project'))
        assertThat(segment.procedureName, is('Some Procedure'))
    }
}
