package com.gap.gradle.plugins.mobile.credentials

import com.gap.gradle.plugins.mobile.CommandRunner
import org.junit.Before
import org.junit.Test

import static com.gap.gradle.plugins.mobile.credentials.GitCryptCredentialProvider.CREDENTIALS_REPOSITORY
import static com.gap.gradle.plugins.mobile.credentials.GitCryptCredentialProvider.SYMMETRIC_KEY
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

public class GitCryptCredentialProviderTest {

    def CommandRunner commandRunner
    def GitCryptCredentialProvider credentialProvider

    @Before
    public void setUp() throws Exception {
        commandRunner = mock(CommandRunner)
        credentialProvider = new GitCryptCredentialProvider(commandRunner)
    }

    @Test
    public void shouldCloneRepo() throws Exception {
        credentialProvider.cloneCredentialsRepo()
        verify(commandRunner).run("git", "clone", "--quiet", "--depth", "1", CREDENTIALS_REPOSITORY, credentialProvider.workingDir.absolutePath)
    }

    @Test
    public void shouldUnlockSecrets() throws Exception {
        credentialProvider.unlockCredentials()
        verify(commandRunner).run(credentialProvider.workingDir, "git-crypt", "unlock", SYMMETRIC_KEY.absolutePath)
    }

    @Test
    public void shouldLockSecrets() throws Exception {
        credentialProvider.lockCredentials()
        verify(commandRunner).run(credentialProvider.workingDir, "git-crypt", "lock")
    }
}
