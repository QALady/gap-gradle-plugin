package com.gap.gradle.airwatch.util
import org.junit.Test

import java.util.concurrent.TimeUnit

import static org.junit.Assert.assertEquals
import static org.mockito.Mockito.*

public class BarrierTest {
    private timeunit = mock(TimeUnit)

    @Test(expected = Barrier.MaxNumberOfTriesReached)
    public void shouldExecuteClosureUpToNumberOfTriesAndThrowException() throws Exception {
        def counter = 0

        new Barrier(10, 1, timeunit).executeUntil { counter++; false }

        assertEquals(10, counter)
        verify(timeunit, times(9)).sleep(1l)
    }

    @Test
    public void shouldStopAndNotSleepWhenClosureReturnsTrue() throws Exception {
        def counter = 0

        new Barrier(10, 1, timeunit).executeUntil { counter++; true }

        assertEquals(1, counter)
        verify(timeunit, never()).sleep(1l)
    }
}