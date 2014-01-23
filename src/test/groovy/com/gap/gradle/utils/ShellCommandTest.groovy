package com.gap.gradle.utils

import static org.junit.rules.ExpectedException.none

import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class ShellCommandTest {

    @Rule
    public ExpectedException exception = none()

    @Test
    void shouldSuccessfullyExecuteShellCommand(){
        new ShellCommand().execute("echo this is a test")
    }

    @Test (expected = IOException)
    void shouldThrowAnException_whenProcessCannotBeFound(){
        new ShellCommand().execute("badcommand")
    }

    @Test
    void shouldThrowAnException_whenProcessReturnsANonZeroExitCode(){
        exception.expectMessage("Command execution failed!!")
        new ShellCommand().execute("ls nonexistentFolder")
    }

}
