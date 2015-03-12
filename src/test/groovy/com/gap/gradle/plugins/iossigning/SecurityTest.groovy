package com.gap.gradle.plugins.iossigning
import com.gap.gradle.plugins.mobile.CommandRunner
import org.junit.Test

import static org.hamcrest.Matchers.arrayContaining
import static org.hamcrest.Matchers.hasSize
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.*

class SecurityTest {
    private static final String SECURITY_TOOL = "/usr/bin/security"

    private commandRunner = mock(CommandRunner)
    private security = new Security(this.commandRunner)
    private keychain = fakeFileWithAbsolutePath()

    @Test
    public void shouldCreateKeychain() throws Exception {
        security.createKeychain("password", keychain)

        verify(commandRunner).run(SECURITY_TOOL, "create-keychain", "-p", "password", keychain.absolutePath)
    }

    @Test
    public void shouldImportCertificate() throws Exception {
        def certFile = fakeFileWithAbsolutePath()

        security.importCertificate(certFile, keychain, "cert pass", "allowed app")

        verify(commandRunner).run(SECURITY_TOOL, "-v", "import", certFile, "-k", keychain.absolutePath, "-P", "cert pass", "-T", "allowed app")
    }

    @Test
    public void shouldDeleteKeychain() throws Exception {
        security.deleteKeychain(keychain)

        verify(commandRunner).run(SECURITY_TOOL, "delete-keychain", keychain.absolutePath)
    }

    @Test
    public void shouldAddKeychainToSystem() throws Exception {
        def runner = new FakeCommandRunner()
        security = new Security(runner)

        def existingKeychain = File.createTempFile("fake", "keychain")
        try {
            security.addKeychain(existingKeychain)

            assertThat(runner.calls, hasSize(2))
            assertThat(runner.calls[0], arrayContaining(SECURITY_TOOL, "list-keychains"))
            assertThat(runner.calls[1], arrayContaining(SECURITY_TOOL, "list-keychains", "-s", existingKeychain.absolutePath))

        } finally {
            existingKeychain.delete()
        }
    }

    @Test
    public void shouldDecodeCmsMessages() throws Exception {
        File inputFile = fakeFileWithAbsolutePath()

        security.decodeCMSMessages(inputFile)

        verify(commandRunner).run(SECURITY_TOOL, "cms", "-D", "-i", inputFile.absolutePath)
    }

    private static File fakeFileWithAbsolutePath() {
        def keychain = mock(File)
        when(keychain.absolutePath).thenReturn("/tmp/fake")
        return keychain
    }

    class FakeCommandRunner extends CommandRunner {
        def calls = new ArrayList<String[]>()

        FakeCommandRunner() {
            super(null)
        }

        @Override
        String run(Object... args) {
            calls << args
            return ""
        }
    }
}
