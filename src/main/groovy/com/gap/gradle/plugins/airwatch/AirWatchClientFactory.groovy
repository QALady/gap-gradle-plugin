package com.gap.gradle.plugins.airwatch

import com.gap.gradle.plugins.mobile.credentials.CredentialProvider

class AirWatchClientFactory {
    AirWatchClient create(Environment environment, CredentialProvider credentialProvider) {
        def credential = credentialProvider.get(environment.credentialName)
        new AirWatchClient(environment.apiHost, credential.username, credential.password, environment.tenantCode)
    }
}
