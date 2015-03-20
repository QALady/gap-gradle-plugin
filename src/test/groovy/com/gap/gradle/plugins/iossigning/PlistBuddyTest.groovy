package com.gap.gradle.plugins.iossigning

import com.gap.gradle.plugins.mobile.CommandRunner
import org.junit.Before
import org.junit.Test

import static org.mockito.Mockito.*

public class PlistBuddyTest {
    public static final String PLISTBUDDY_TOOL = "/usr/libexec/PlistBuddy"

    private CommandRunner commandRunner
    private PlistBuddy plistBuddy

    @Before
    public void setUp() throws Exception {
        commandRunner = mock(CommandRunner)
        plistBuddy = new PlistBuddy(commandRunner)
    }

    @Test
    public void shouldPrintValueOfSpecifiedEntry() throws Exception {
        def entryName = "someKey"
        def plistFile = fakeFileWithAbsolutePath("/path/to/some/plist")

        plistBuddy.printEntry(entryName, plistFile)

        verify(commandRunner).run(PLISTBUDDY_TOOL, "-c", "Print ${entryName}", plistFile.absolutePath)
    }

    @Test
    public void shouldPrintValueOfSpecifiedEntryAndOutputAsXml() throws Exception {
        def entryName = "someKey"
        def plistFile = fakeFileWithAbsolutePath("/path/to/some/plist")

        plistBuddy.printEntry(entryName, plistFile, true)

        verify(commandRunner).run(PLISTBUDDY_TOOL, "-c", "Print ${entryName}", plistFile.absolutePath, "-x")
    }

    private static File fakeFileWithAbsolutePath(String filePath) {
        def keychain = mock(File)
        when(keychain.absolutePath).thenReturn(filePath)
        return keychain
    }
}
