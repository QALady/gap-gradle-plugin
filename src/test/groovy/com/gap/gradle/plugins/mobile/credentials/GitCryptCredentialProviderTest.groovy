package com.gap.gradle.plugins.mobile.credentials

import com.gap.gradle.plugins.mobile.CommandRunner
import org.junit.Before
import org.junit.Test

import static com.gap.gradle.plugins.mobile.credentials.GitCryptCredentialProvider.*
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

        verify(commandRunner).run("git", "clone", "--quiet", "--depth", "1", CREDENTIALS_REPOSITORY, WORKING_DIR.absolutePath)
    }

    @Test
    public void shouldUnlockSecrets() throws Exception {
        def gitCrypt = mock(GitCrypt)
        def fileParser = mock(CredentialFileParser)

        credentialProvider.gitCrypt = gitCrypt
        credentialProvider.credentialFileParser = fileParser
        credentialProvider.get("foo")

        verify(gitCrypt).unlock(WORKING_DIR, SYMMETRIC_KEY)
        verify(gitCrypt).lock(WORKING_DIR)
        verify(fileParser).parse("foo")
    }
}
