package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.tasks.GcovAnalysisTask
import com.gap.gradle.plugins.xcode.tasks.ReplaceTokensInFile
import com.gap.gradle.plugins.xcode.tasks.TransformJUnitXmlReportToHTMLTask
import com.gap.gradle.plugins.xcode.tasks.BuildHybridSDKTask
import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import org.gradle.internal.reflect.Instantiator
import com.gap.gradle.plugins.mobile.CommandRunner

import javax.inject.Inject

import org.apache.commons.logging.LogFactory

import static com.gap.gradle.plugins.xcode.SigningIdentity.DEFAULT_DEVELOPMENT
import static com.gap.gradle.plugins.xcode.SigningIdentity.DEFAULT_DISTRIBUTION

class GapXcodePlugin implements Plugin<Project> {

    private static final logger = LogFactory.getLog(GapXcodePlugin)
    private final Instantiator instantiator
    private Project project
    private XcodeExtension extension

    @Inject
    public GapXcodePlugin(Instantiator instantiator) {
        this.instantiator = instantiator
    }

    @Override
    void apply(Project project) {
        this.project = project
        this.extension = project.extensions.create('xcode', XcodeExtension, instantiator, project)
        this.extension.signing.add(DEFAULT_DEVELOPMENT)
        this.extension.signing.add(DEFAULT_DISTRIBUTION)

        this.project.plugins.apply('org.openbakery.xcode-plugin')
        this.project.configurations.create('airwatchConfig')

        configureWorkspaceAndScheme()
        configureExistingTasks()
        createNewTasks()
        configureTaskGraph()

        project.afterEvaluate {
            project.version = extension.archive.version
        }
    }

    private configureWorkspaceAndScheme() {
        this.project.afterEvaluate {
            this.project.xcodebuild {
                workspace = extension.workspace
                scheme = extension.scheme
            }
        }
    }

    private configureExistingTasks() {
        project.tasks['xcodetest'].doLast {
            project.tasks['transformJUnitXmlReportToHTML'].execute()
        }

        project.tasks['xcodebuild'].dependsOn('keychainCreate')
        project.tasks['xcodebuild'].dependsOn('provisioningInstall')
        project.tasks['xcodebuild'].finalizedBy('replaceTokensInSettingsBundle')

        project.tasks['package'].doLast {
            moveGeneratedArtifactsToArtifactsDirectory()
        }

        // Xcode gradle plugin version 0.10 moved the IPA creation and signing to the 'package' task.
        // To keep projects backwards compatible we're running package after 'archive' runs.
        project.tasks['archive'].doLast {
            project.tasks['package'].execute()
        }

        project.tasks['uploadArchives'].dependsOn('airwatchConfigZip')

        // Xcode gradle plugin version 0.10 renamed the clean tasks.
        // Adding aliases to keep backwards compatibility.
        project.task('keychain-clean').dependsOn('keychainClean')
        project.task('provisioning-clean').dependsOn('provisioningClean')
    }

    private createNewTasks() {
        project.task('replaceTokensInSettingsBundle', type: ReplaceTokensInFile) {
            doFirst {
                targetFilePath pathToRootPlist()
                tokensToReplace = ['@version@'    : extension.archive.version,
                                   '@scmRevision@': extension.archive.scmRevision]
            }
            onlyIf { new File(pathToRootPlist()).exists() }
        }

        project.task('airwatchConfigZip', type: Zip)

        project.task('buildHybridSDK', type: BuildHybridSDKTask) {
            this.project.afterEvaluate {
                sdkScheme this.project.xcodebuild.scheme
            }
        }

        project.task('transformJUnitXmlReportToHTML', type: TransformJUnitXmlReportToHTMLTask) {
            sourceFilePath "${project.buildDir}/test/test-results.xml"
            targetFilePath "${project.buildDir}/test/index.html"
        }

        project.task('gcovAnalyze', type: GcovAnalysisTask) {
            fileNamePattern '**/*.gcda'
            reportsDir project.file("${project.buildDir}/reports/gcov")
        }
    }

    private configureTaskGraph() {

        project.gradle.taskGraph.whenReady { taskGraph ->

            if (taskGraph.hasTask(':xcodetest')) {
                extension.test.validate()
                def testConfig = extension.test

                project.xcodebuild {
                    scheme = testConfig.scheme
                    // Openbakery xocde plugin 0.13 requires target to be specifed
                    // To make gap-xcode plugin backward compatible 'scheme' is assigned to 'target' if not specified
                    if (testConfig.target == null) {
                        target = testConfig.scheme
                    }
                    else {
                        target = testConfig.target
                    }
                    destination {
                        platform = testConfig.destination.platform
                        name = testConfig.destination.name
                        os = testConfig.destination.os
                    }

                    configuration = 'Debug'
                    additionalParameters = ['VALID_ARCHS=i386', 'ONLY_ACTIVE_ARCH=NO', 'GCC_GENERATE_TEST_COVERAGE_FILES=YES', 'GCC_INSTRUMENT_PROGRAM_FLOW_ARCS=YES']
                }
            }

            if (taskGraph.hasTask(':xcodebuild')) {
                extension.build.validate()
                def buildConfig = extension.build

                project.xcodebuild {
                    productName = buildConfig.productName
                    target = buildConfig.target 
                    simulator = (buildConfig.sdk == 'iphoneos')? false : true
                    if(!simulator){
                        type = "iOS" // This parameter is replacement of sdk parameter in 0.10.3 plugin
                    }
                    configuration = buildConfig.configuration
                    symRoot = targetOutputDir()

                    def signIdentity = buildConfig.signingIdentity
                    if (signIdentity != null) {
                        signing {
                            identity = signIdentity.description
                            certificateURI = signIdentity.certificateURI
                            certificatePassword = signIdentity.certificatePassword
                            mobileProvisionURI = signIdentity.mobileProvisionURI
                        }
                    }

                    if (simulator) {
                        additionalParameters = ['VALID_ARCHS=i386 x86_64', 'ONLY_ACTIVE_ARCH=NO']
                    }
                }
            }

            if (taskGraph.hasTask(':archive')) {
                extension.archive.validate()

                project.infoplist {
                    version = extension.archive.version
                    shortVersionString = extension.archive.shortVersionString
                }
            }

            if (taskGraph.hasTask(':uploadArchives')) {

                project.fileTree(dir: getArtifactsDir()).each { file ->
                    logger.info("Found artifact ${file.absolutePath}")

                    (file.name =~ /^(\w+?)-(.*)\.(ipa|zip)$/).each { match, artifactName, artifactClassifier, type ->

                        printf "Name: %s\nClassifier: %s\nType: %s\n\n", artifactName, artifactClassifier, type

                        project.artifacts.add("archives", file) {
                            name artifactName
                            classifier artifactClassifier
                        }
                    }
                }

                def target = extension.build.target
                def airwatchConfigForTarget = "airwatchConfig/${target}"

                if (project.file(airwatchConfigForTarget).exists()) {
                    project.airwatchConfigZip.from airwatchConfigForTarget
                    project.artifacts {
                        archives(project.airwatchConfigZip) {
                            name target
                            classifier 'airwatchConfig'
                        }
                    }
                }
            }
        }
    }

    private File targetOutputDir() {
        project.file("${project.buildDir}/${extension.build.productName}")
    }

    private void moveGeneratedArtifactsToArtifactsDirectory() {
        def codeSign = extension.build.signingIdentity
        def configuration = project.xcodebuild.configuration
        def sdk = extension.build.sdk
        def productName = project.xcodebuild.productName

        def artifactsDir = getArtifactsDir()

        artifactsDir.mkdirs()

        if (sdk == 'iphoneos') {
            // Zip file with the debug symbols
            project.ant.zip(destfile: new File(artifactsDir, artifactFileName(productName, sdk, codeSign, 'zip')).toString()) {
                zipfileset(prefix: "${productName}.app.dSYM", dir: "${targetOutputDir()}/${configuration}-${sdk}/${productName}.app.dSYM")
            }

            FileUtils.copyFile(new File(project.buildDir, "package/${productName}.ipa"), new File(artifactsDir, artifactFileName(productName, sdk, codeSign, 'ipa')))
        } else {
            FileUtils.copyFile(new File(project.buildDir, "archive/${productName}.zip"), new File(artifactsDir, artifactFileName(productName, sdk, codeSign, 'zip')))
        }
    }

    private File getArtifactsDir() {
        new File(project.buildDir, 'artifacts')
    }

    private static String artifactFileName(String productName, String sdk, SigningIdentity codeSign, String extension) {
        (codeSign != null) ? "${productName}-${sdk}-${codeSign.name}.${extension}" : "${productName}-${sdk}.${extension}"
    }

    private String pathToRootPlist() {
        def configuration = extension.build.configuration
        def sdk = extension.build.sdk
        def appName = extension.build.productName

        "${targetOutputDir()}/${configuration}-${sdk}/${appName}.app/Settings.bundle/Root.plist"
    }

    void signEmbeddedFramework() {
        def sdk = project.xcodebuild.simulator
        def productName = project.xcodebuild.productName
        def target = project.xcodebuild.target
        def type = project.xcodebuild.type

        File frameworksDirectory = new File(project.buildDir, "archive/${target}.xcarchive/Products/Applications/${productName}.app/Frameworks")

        //if (frameworksDirectory.exists() && sdk == 'iphoneos') {
          if (frameworksDirectory.exists() && !sdk) {
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".dylib") || name.toLowerCase().endsWith(".framework");
                }
            };
            def commandRunner = new CommandRunner(project)

            for (File file in frameworksDirectory.listFiles(filter)) {
                commandRunner.run("/usr/bin/codesign",
                        "--force",
                        "--sign",project.xcodebuild.getSigning().getIdentity(),
                        "--verbose",
                        file.absolutePath,
                        "--keychain",
                        project.xcodebuild.signing.keychainPathInternal.absolutePath)
            }
        }

    }
}
