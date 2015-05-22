package com.gap.gradle.plugins.mobile.credentials

import com.gap.pipeline.ec.CommanderClient

class EctoolCredentialProvider implements CredentialProvider {
    private static final CREDENTIAL_PATH = "/projects/WM Credentials/credentials"

    private CommanderClient commanderClient

    EctoolCredentialProvider(CommanderClient commanderClient = new CommanderClient()) {
        this.commanderClient = commanderClient
    }

    @Override
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
