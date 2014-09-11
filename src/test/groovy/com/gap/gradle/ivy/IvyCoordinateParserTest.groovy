package com.gap.gradle.ivy

import com.gap.pipeline.utils.IvyCoordinateParser
import org.junit.Test

import static org.hamcrest.CoreMatchers.is
import static org.junit.Assert.assertThat

/**
 * Created by gopichand on 9/11/14.
 */
class IvyCoordinateParserTest {

    @Test
    void testGetAppName(){
        IvyCoordinateParser ivyCoordinateParser = new IvyCoordinateParser()
        //assertTrue(ivyCoordinateParser.getAppName("com.gapinc.selling.stores.apps.snapscan") == "snapscan")
        assertThat(ivyCoordinateParser.getAppName("com.gapinc.selling.stores.apps.snapscan"), is("snapscan"))


    }

    @Test
    void testGetCookbookName(){
        IvyCoordinateParser ivyCoordinateParser = new IvyCoordinateParser()
        assertThat(ivyCoordinateParser.getCookbookName("com.gapinc.selling.stores.apps.snapscan.infra"), is("snapscan"))
    }

    @Test
    void testGetCookbookVersion(){
        IvyCoordinateParser ivyCoordinateParser = new IvyCoordinateParser()
        assertThat(ivyCoordinateParser.getCookbookVersion("0.2.5.20140709001027"), is("0.2.5"))
    }
}
