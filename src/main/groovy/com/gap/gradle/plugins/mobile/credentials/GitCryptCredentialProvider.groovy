package com.gap.gradle.plugins.mobile.credentials

import com.gap.gradle.plugins.mobile.CommandRunner
import org.gradle.api.GradleException

class GitCryptCredentialProvider implements CredentialProvider {

    public static final String CREDENTIALS_REPOSITORY = "http://github.gapinc.dev/mpl/credentials.git"
    public static final File CRYPT_KEY = new File(System.getProperty("user.home"), ".git-crypt/mpl_secret_key")

    private CommandRunner commandRunner
    private File workingDir

    GitCryptCredentialProvider(CommandRunner commandRunner) {
        this.commandRunner = commandRunner
        this.workingDir = new File(System.getProperty("java.io.tmpdir"), "credentials")
    }

    @Override
    Credential get(String credentialName) {
        if (!CRYPT_KEY.exists()) {
            throw new GradleException("Unable to load symmetric key from ${CRYPT_KEY.absolutePath}")
        }

        cloneCredentialsRepo()

        unlockCredentials()

        Credential credential = new CredentialFileParser(workingDir).parse(credentialName)

        lockCredentials()

        return credential
    }

    void lockCredentials() {
        commandRunner.run(workingDir, "git-crypt", "lock")
        workingDir.deleteDir()
    }

    void unlockCredentials() {
        commandRunner.run(workingDir, "git-crypt", "unlock", CRYPT_KEY.absolutePath)
    }

    void cloneCredentialsRepo() {
        workingDir.deleteDir()
        commandRunner.run("git", "clone", "--quiet", "--depth", "1", CREDENTIALS_REPOSITORY, workingDir.absolutePath)
    }
}
