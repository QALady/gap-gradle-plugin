package com.gap.gradle.plugins.iossigning

import com.gap.gradle.plugins.mobile.CommandRunner
import com.gap.gradle.plugins.xcode.SigningIdentity
import org.apache.commons.io.FileUtils

class IpaPackage {
    private static final String CODESIGN_TOOL = "/usr/bin/codesign"

    private final File ipaFile
    private final File workingDir
    private final File ipaAppDir
    private final File ipaExplodedDir
    private final CommandRunner commandRunner
    private final Zipper zipper

    def IpaPackage(File ipaFile, File workingDir, Zipper zipper, CommandRunner commandRunner) {
        this.ipaFile = ipaFile
        this.workingDir = workingDir
        this.zipper = zipper
        this.commandRunner = commandRunner

        ipaExplodedDir = explodedIpaPackageDir()
        ipaAppDir = appDirInsideIpaPayload()
    }

    public void replaceEmbeddedProvision(File newMobileProvision) {
        FileUtils.copyFile(newMobileProvision, new File(ipaAppDir, "embedded.mobileprovision"))
    }

    public File resign(SigningIdentity signingIdentity, Keychain keychain, File entitlements) {
        commandRunner.run(CODESIGN_TOOL,
                "--force", "--entitlements", entitlements, "--sign", signingIdentity.description,
                "--verbose", ipaAppDir.absolutePath, "--keychain", keychain.file.absolutePath)

        def resignedIpa = new File(workingDir, resignedIpaName())
        resignedIpa.delete()

        zipper.zip(ipaExplodedDir, resignedIpa)

        return resignedIpa
    }

    private File explodedIpaPackageDir() {
        def ipaExplodedDir = new File(workingDir, "exploded-ipa")
        ipaExplodedDir.mkdirs()

        zipper.unzip(ipaFile, ipaExplodedDir)

        return ipaExplodedDir
    }

    private File appDirInsideIpaPayload() {
        def payloadDir = new File(ipaExplodedDir, "Payload")
        return payloadDir.listFiles()[0]
    }

    private String resignedIpaName() {
        ipaFile.name.replaceFirst(/\.ipa$/, "-resigned.ipa")
    }
}
