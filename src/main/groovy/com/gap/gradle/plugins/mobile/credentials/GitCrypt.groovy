package com.gap.gradle.plugins.mobile.credentials

import com.gap.gradle.plugins.mobile.CommandRunner
import org.gradle.api.GradleException

class GitCrypt {

    private static final String GITCRYPT_TOOL = "git-crypt"

    private final CommandRunner commandRunner

    GitCrypt(CommandRunner commandRunner) {
        this.commandRunner = commandRunner
    }

    void lock(File workingDir) {
        commandRunner.run(workingDir, GITCRYPT_TOOL, "lock")
    }

    void unlock(File workingDir, File symmetricKey) {
        if (!symmetricKey.exists()) {
            throw new GradleException("Unable to load symmetric key from ${symmetricKey.absolutePath}")
        }

        commandRunner.run(workingDir, GITCRYPT_TOOL, "unlock", symmetricKey.absolutePath)
    }
}
