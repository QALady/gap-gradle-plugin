package com.gap.pipeline.utils

import static org.hamcrest.CoreMatchers.is
import static org.junit.Assert.assertThat
import static org.junit.rules.ExpectedException.none

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class IvyCoordinateParserTest {

    IvyCoordinateParser parser
    String coordinates

    @Rule
    public final ExpectedException exception = none()

    @Before
    public void setUp(){
        parser = new IvyCoordinateParser()
    }

    @Test
    public void shouldReturnIvyCoordinateObject(){
        def coordinates = parser.parse("some.ivy.group:artifactName:12345")
        assertThat(coordinates, is([name: 'artifactName', group: 'some.ivy.group', version: '12345']))
    }

    @Test
    public void shouldThrowExceptionIfTheFormatIsInvalid(){
        exception.expect(IllegalArgumentException)
        exception.expectMessage("The coordinates 'some.ivy.group' is of invalid format. The format should be <groupname>:<modulename>:<version>")
        parser.parse("some.ivy.group")
    }

}