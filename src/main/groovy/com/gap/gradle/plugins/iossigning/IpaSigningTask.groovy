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

        def keychain = new Keychain(new Security(commandRunner))
        keychain.importCertificate(certificate, signingIdentity.certificatePassword, CODESIGN_TOOL_PATH)

        try {
            def ipaPackage = new IpaPackage(resolvedArtifact.file, signingDir, new Zipper(project), commandRunner)
            ipaPackage.replaceEmbeddedProvision(mobileProvision)
            File newSignedIpa = ipaPackage.resign(signingIdentity, keychain)
            output = newSignedIpa

        } finally {
            keychain.destroy()
        }
    }
}
