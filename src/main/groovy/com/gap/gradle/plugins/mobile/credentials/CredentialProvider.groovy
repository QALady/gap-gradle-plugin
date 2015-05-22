package com.gap.gradle.plugins.mobile.credentials

interface CredentialProvider {
    Credential get(String credentialName)
}
