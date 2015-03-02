package com.gap.gradle.plugins.iossigning

class Keychain {
    private final keychainFile = File.createTempFile("ios-signing", "keychain")
    private final Security security

    Keychain(Security security) {
        this.security = security

        keychainFile.delete() // Just use the temporary filename

        security.createKeychain("ios-signing-password", keychainFile)
        security.addKeychain(keychainFile)
    }

    def importCertificate(File certificate, String password, String allowedAppPath) {
        security.importCertificate(certificate, keychainFile, password, allowedAppPath)
    }

    def destroy() {
        security.deleteKeychain(keychainFile)
    }

    def getFile() {
        return keychainFile
    }
}
