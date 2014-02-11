package com.gap.gradle.utils

import static org.junit.Assert.assertEquals
import static org.junit.rules.ExpectedException.none

import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class ShellCommandTest {

    @Rule
    public final ExpectedException exception = none()

    @Test
    void shouldSuccessfullyExecuteShellCommand(){
        def output = new ShellCommand().execute("echo this is a test", null)
        assertEquals("this is a test", output.trim());
    }

    @Test (expected = IOException)
    void shouldThrowAnException_whenProcessCannotBeFound(){
        new ShellCommand().execute("badcommand", null)
    }

    @Test
    void shouldThrowAnException_whenProcessReturnsANonZeroExitCode(){
        exception.expect(ShellCommandException)
        new ShellCommand().execute("ls nonexistentFolder", null)
    }
}
