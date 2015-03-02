package com.gap.gradle.plugins.iossigning

import org.junit.Test
import org.mockito.Mockito

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

class KeychainTest {
    private security = mock(Security)
    private keychain = new Keychain(this.security)

    @Test
    public void shouldCreateTemporaryKeychainAndAddToSystem() throws Exception {
        verify(this.security).createKeychain(Mockito.anyString(), Mockito.eq(this.keychain.file))
        verify(this.security).addKeychain(this.keychain.file)
    }

    @Test
    public void shouldDestroyTheKeychain() throws Exception {
        keychain.destroy()

        verify(security).deleteKeychain(keychain.file)
    }

    @Test
    public void shouldImportCertificateProtectedByPassword() throws Exception {
        def certFile = mock(File)

        keychain.importCertificate(certFile, "password", "path to app")

        verify(security).importCertificate(certFile, keychain.file, "password", "path to app")
    }
}
