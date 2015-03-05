package com.gap.gradle.plugins.iossigning

import com.gap.gradle.airwatch.util.CommandRunner
import org.junit.Before
import org.junit.Test

import static org.mockito.Mockito.*

public class ZipperTest {
    public static final String ZIP_TOOL = "/usr/bin/zip"
    public static final String UNZIP_TOOL = "/usr/bin/unzip"

    private CommandRunner commandRunner
    private Zipper zipper

    @Before
    public void setUp() throws Exception {
        commandRunner = mock(CommandRunner)

        zipper = new Zipper(commandRunner)
    }

    @Test
    public void shouldCallSystemZipTool() throws Exception {
        def baseDir = fakeFileWithAbsolutePath("/path/to/base/dir")
        def destinationZip = fakeFileWithAbsolutePath("/path/to/some.zip")

        zipper.zip(baseDir, destinationZip)

        verify(commandRunner).run(baseDir, ZIP_TOOL, "/path/to/some.zip", "-r", ".")
    }

    @Test
    public void shouldCallSystemUnzipTool() throws Exception {
        def zipFile = fakeFileWithAbsolutePath("/path/to/some.zip")
        def destinationDir = fakeFileWithAbsolutePath("/path/to/destination/dir")

        zipper.unzip(zipFile, destinationDir)

        verify(commandRunner).run(UNZIP_TOOL, zipFile.absolutePath, "-d", destinationDir.absolutePath)
    }

    private static File fakeFileWithAbsolutePath(String filePath) {
        def fileMock = mock(File)
        when(fileMock.absolutePath).thenReturn(filePath)
        return fileMock
    }
}
