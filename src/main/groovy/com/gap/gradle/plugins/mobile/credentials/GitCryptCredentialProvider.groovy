package com.gap.gradle.plugins.mobile.credentials

import com.gap.gradle.plugins.mobile.CommandRunner

class GitCryptCredentialProvider implements CredentialProvider {

    public static final String CREDENTIALS_REPOSITORY = "http://github.gapinc.dev/mpl/credentials.git"
    public static final File SYMMETRIC_KEY = new File(System.getProperty("user.home"), ".git-crypt/credentials_key")
    public static final File WORKING_DIR = new File(System.getProperty("java.io.tmpdir"), "credentials")

    private final CommandRunner commandRunner
    private GitCrypt gitCrypt
    private CredentialFileParser credentialFileParser

    GitCryptCredentialProvider(CommandRunner commandRunner) {
        this.commandRunner = commandRunner
        this.gitCrypt = new GitCrypt(commandRunner)
        this.credentialFileParser = new CredentialFileParser(WORKING_DIR)
    }

    @Override
    Credential get(String credentialName) {
        cloneCredentialsRepo()

        gitCrypt.unlock(WORKING_DIR, SYMMETRIC_KEY)

        Credential credential = credentialFileParser.parse(credentialName)

        gitCrypt.lock(WORKING_DIR)

        WORKING_DIR.deleteDir()

        return credential
    }

    void cloneCredentialsRepo() {
        WORKING_DIR.deleteDir()
        commandRunner.run("git", "clone", "--quiet", "--depth", "1", CREDENTIALS_REPOSITORY, WORKING_DIR.absolutePath)
    }

    void setGitCrypt(GitCrypt gitCrypt) {
        this.gitCrypt = gitCrypt
    }

    void setCredentialFileParser(CredentialFileParser credentialFileParser) {
        this.credentialFileParser = credentialFileParser
    }
}
