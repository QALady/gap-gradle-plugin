package com.gap.gradle.plugins.mobile.credentials

import com.gap.gradle.plugins.mobile.CommandRunner
import org.gradle.api.GradleException
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.fail
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

public class GitCryptTest {

    CommandRunner commandRunner
    GitCrypt gitCrypt
    File testDir

    @Before
    public void setUp() throws Exception {
        commandRunner = mock(CommandRunner)
        gitCrypt = new GitCrypt(commandRunner)
        testDir = File.createTempFile("working", "dir")
    }

    @Test
    public void shouldUnlockRepository() throws Exception {
        def fakeKey = File.createTempFile("fake", "key")
        gitCrypt.unlock(testDir, fakeKey)
        verify(commandRunner).run(testDir, "git-crypt", "unlock", fakeKey.absolutePath)
    }

    @Test
    public void shouldLockRepository() throws Exception {
        gitCrypt.lock(testDir)
        verify(commandRunner).run(testDir, "git-crypt", "lock")
    }

    @Test(expected = GradleException)
    public void shouldThrowExceptionIfSymmetricKeyNotExists() throws Exception {
        gitCrypt.unlock(testDir, new File(""))
    }
}
