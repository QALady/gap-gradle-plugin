package com.gap.gradle.airwatch

import com.gap.pipeline.ec.CommanderClient

class CredentialProvider {
    private static final CREDENTIAL_PATH = "/projects/WM Credentials/credentials"

    private CommanderClient commanderClient

    CredentialProvider(CommanderClient commanderClient = new CommanderClient()) {
        this.commanderClient = commanderClient
    }

    Credential get(String credentialName) {
        new Credential(getUsername(credentialName), getPassword(credentialName))
    }

    private String getUsername(String credentialName) {
        commanderClient.getCredential("${CREDENTIAL_PATH}/$credentialName", 'userName')
    }

    private String getPassword(String credentialName) {
        commanderClient.getCredential("${CREDENTIAL_PATH}/$credentialName", 'password')
    }
}
