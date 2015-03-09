package com.gap.gradle.plugins.iossigning

import com.gap.gradle.airwatch.ArtifactSpec
import com.gap.gradle.airwatch.util.CommandRunner
import com.gap.gradle.plugins.iossigning.exceptions.ArtifactNotFoundException
import com.gap.gradle.plugins.mobile.ArchivesArtifactFinder
import com.gap.gradle.plugins.xcode.SigningIdentity
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.reflect.Instantiator
import org.gradle.util.ConfigureUtil

import javax.inject.Inject

import static com.gap.gradle.plugins.xcode.SigningIdentity.DEFAULT_DEVELOPMENT
import static com.gap.gradle.plugins.xcode.SigningIdentity.DEFAULT_DISTRIBUTION

class IpaSigningTask extends DefaultTask {

    private static final String CODESIGN_TOOL_PATH = "/usr/bin/codesign"

    private final FileDownloader downloader
    private final CommandRunner commandRunner
    private final Security security

    SigningIdentity signingIdentity
    ArtifactSpec artifact
    File output

    final NamedDomainObjectSet<SigningIdentity> signing

    @Inject
    IpaSigningTask(Instantiator instantiator) {
        setOnlyIf { hasRequiredParameters() }

        signing = project.container(SigningIdentity)
        signing.add(DEFAULT_DEVELOPMENT)
        signing.add(DEFAULT_DISTRIBUTION)

        artifact = instantiator.newInstance(ArtifactSpec)
        downloader = instantiator.newInstance(FileDownloader, project)

        // There's a bug in Gradle's Instantiator that causes a "missing property 'project'"
        // when calling commandRunner.run(...), that's why we're using a "new" here.
        commandRunner = new CommandRunner(project)
        security = new Security(commandRunner)
    }

    def hasRequiredParameters() {
        signingIdentity != null && artifact.hasAtLeastOneSpecDefined()
    }

    def artifact(Closure closure) {
        ConfigureUtil.configure(closure, artifact)
    }

    @TaskAction
    def signIpa() {
        def resolvedArtifact = new ArchivesArtifactFinder(project).find(artifact)
        if (resolvedArtifact == null) {
            throw new ArtifactNotFoundException(artifact)
        }

        def signingDir = new File(project.buildDir, "gap-ios-signing")
        signingDir.mkdirs()

        def certificate = downloader.download(signingIdentity.certificateURI, signingDir)
        def mobileProvision = downloader.download(signingIdentity.mobileProvisionURI, signingDir)

        def keychain = new Keychain(security)
        keychain.importCertificate(certificate, signingIdentity.certificatePassword, CODESIGN_TOOL_PATH)

        try {
            def ipaPackage = new IpaPackage(resolvedArtifact.file, signingDir, new Zipper(commandRunner), commandRunner)
            ipaPackage.replaceEmbeddedProvision(mobileProvision)

            def entitlements = getEntitlements(mobileProvision, signingDir)

            output = ipaPackage.resign(signingIdentity, keychain, entitlements)
        } finally {
            keychain.destroy()
        }
    }

    private File getEntitlements(File provisioningProfile, File outputDir) {
        def decodedCms = security.decodeCMSMessages(provisioningProfile)

        def plainProfile = new File(outputDir, "plain-provisioning-profile.plist")
        plainProfile.write(decodedCms)

        def entitlements = new PlistBuddy(commandRunner).printEntry(":Entitlements", plainProfile)

        def entitlementsFile = new File(outputDir, "entitlements.plist")
        entitlementsFile.write(entitlements)

        entitlementsFile
    }
}
