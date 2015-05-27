package com.gap.gradle.plugins.mobile.credentials

import com.gap.gradle.plugins.mobile.CommandRunner
import org.gradle.api.GradleException

class GitCryptCredentialProvider implements CredentialProvider {

    public static final String CREDENTIALS_REPOSITORY = "http://github.gapinc.dev/mpl/credentials.git"
    public static final File SYMMETRIC_KEY = new File(System.getProperty("user.home"), ".git-crypt/credentials_key")
    public static final File WORKING_DIR = new File(System.getProperty("java.io.tmpdir"), "credentials")

    private final CommandRunner commandRunner

    GitCryptCredentialProvider(CommandRunner commandRunner) {
        this.commandRunner = commandRunner
    }

    @Override
    Credential get(String credentialName) {
        if (!SYMMETRIC_KEY.exists()) {
            throw new GradleException("Unable to load symmetric key from ${SYMMETRIC_KEY.absolutePath}")
        }

        cloneCredentialsRepo()

        unlockCredentials()

        Credential credential = new CredentialFileParser(WORKING_DIR).parse(credentialName)

        lockCredentials()

        return credential
    }

    void lockCredentials() {
        commandRunner.run(WORKING_DIR, "git-crypt", "lock")
        WORKING_DIR.deleteDir()
    }

    void unlockCredentials() {
        commandRunner.run(WORKING_DIR, "git-crypt", "unlock", SYMMETRIC_KEY.absolutePath)
    }

    void cloneCredentialsRepo() {
        WORKING_DIR.deleteDir()
        commandRunner.run("git", "clone", "--quiet", "--depth", "1", CREDENTIALS_REPOSITORY, WORKING_DIR.absolutePath)
    }
}
