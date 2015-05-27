package com.gap.gradle.plugins.mobile.credentials

import org.gradle.api.GradleException

class CredentialFileParser {

    public static final String CREDENTIAL_PATTERN = ~/username:\s(.*?)\s+?password:\s(.*?)\s+?/

    private final File workingDir

    CredentialFileParser(File workingDir) {
        this.workingDir = workingDir
    }

    Credential parse(String credentialName) {
        def credentialFile = getCredentialFile(credentialName)

        def matcher = (credentialFile.text =~ CREDENTIAL_PATTERN)

        if (!matcher.matches()) {
            throw new GradleException("Invalid format! A credential must match the following pattern: /${CREDENTIAL_PATTERN}/")
        }

        String username = matcher.group(1)
        String password = matcher.group(2)

        return new Credential(username, password)
    }

    private File getCredentialFile(String credentialName) {
        def file = new File(workingDir, "${credentialName}.credential")

        if (!file.exists()) {
            throw new GradleException("Unable to find \"$credentialName\" credential (${file.absolutePath}).")
        }

        file
    }
}
