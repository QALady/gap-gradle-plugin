package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.tasks.GcovAnalysisTask
import com.gap.gradle.plugins.xcode.tasks.ReplaceTokensInFile
import com.gap.gradle.plugins.xcode.tasks.TransformJUnitXmlReportToHTMLTask
import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import org.gradle.internal.reflect.Instantiator

import javax.inject.Inject

class GapXcodePlugin implements Plugin<Project> {

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
        this.extension.signing.add(developmentIdentity())
        this.extension.signing.add(distributionIdentity())

        this.project.plugins.apply('xcode')
        this.project.configurations.create('airwatchConfig')

        configureWorkspaceAndScheme()
        configureExistingTasks()
        createNewTasks()
        configureTaskGraph()
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
        project.tasks['test'].finalizedBy('transformJUnitXmlReportToHTML')

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
        }

        project.task('airwatchConfigZip', type: Zip)

        project.task('transformJUnitXmlReportToHTML', type: TransformJUnitXmlReportToHTMLTask) {
            sourceFilePath "${project.buildDir}/test/test-results.xml"
            targetFilePath "${project.buildDir}/test/index.html"
        }

        project.task('gcovAnalyze', type: GcovAnalysisTask, dependsOn: 'test') {
            fileNamePattern '**/*.gcda'
            reportsDir project.file("${project.buildDir}/reports/gcov")
        }
    }

    private configureTaskGraph() {

        project.gradle.taskGraph.whenReady { taskGraph ->

            if (taskGraph.hasTask(':test')) {
                extension.test.validate()
                def testConfig = extension.test

                project.xcodebuild {
                    scheme = testConfig.scheme

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
                def signIdentity = buildConfig.signingIdentity

                project.xcodebuild {
                    productName = buildConfig.productName
                    target = buildConfig.target
                    sdk = buildConfig.sdk
                    configuration = buildConfig.configuration
                    symRoot = targetOutputDir(signIdentity.name)

                    signing {
                        identity = signIdentity.description
                        certificateURI = signIdentity.certificateURI
                        certificatePassword = signIdentity.certificatePassword
                        mobileProvisionURI = signIdentity.mobileProvisionURI
                    }

                    if (sdk == 'iphonesimulator') {
                        additionalParameters = ['VALID_ARCHS=i386', 'ONLY_ACTIVE_ARCH=NO']
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
                def productName = extension.build.productName
                def target = extension.build.target

                extension.archive.validate()
                project.version = extension.archive.version

                project.artifacts {
                    def chosenIdentity = extension.build.signingIdentity.name
                    archives name: productName, classifier: 'iphoneos', file: fullPathToArtifact(chosenIdentity, 'iphoneos', 'ipa')
                    archives name: productName, classifier: 'iphoneos', file: fullPathToArtifact(chosenIdentity, 'iphoneos', 'zip')
                    archives name: productName, classifier: 'iphonesimulator', file: fullPathToArtifact(chosenIdentity, 'iphonesimulator', 'zip')

                    def developmentIdentity = extension.signing.development.name
                    archives name: productName, classifier: 'iphoneos-dev', file: fullPathToArtifact(developmentIdentity, 'iphoneos', 'ipa')
                    archives name: productName, classifier: 'iphoneos-dev', file: fullPathToArtifact(developmentIdentity, 'iphoneos', 'zip')
                }

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

    private static SigningIdentity developmentIdentity() {
        new SigningIdentity('development').with {
            description = 'iPhone Developer: Alan Orchaton (DY23J3NF52)'
            certificateURI = 'http://github.gapinc.dev/mpl/ios-code-signing/raw/master/DevPrivateKey.p12'
            certificatePassword = 'bollinger!'
            mobileProvisionURI = 'http://github.gapinc.dev/mpl/ios-code-signing/raw/master/GapDevelopment.mobileprovision'
            return it
        }
    }

    private static SigningIdentity distributionIdentity() {
        new SigningIdentity('distribution').with {
            description = 'iPhone Distribution: Gap Inc.'
            certificateURI = 'http://github.gapinc.dev/mpl/ios-code-signing/raw/master/InternalDistKeyv2.p12'
            certificatePassword = 'jlohr1'
            mobileProvisionURI = 'http://github.gapinc.dev/mpl/ios-code-signing/raw/master/GapInternalDistribution.mobileprovision'
            return it
        }
    }

    private File targetOutputDir(identityName) {
        def appName = extension.build.productName
        def basePath = project.buildDir

        project.file("${basePath}/sym-${identityName}/${appName}")
    }

    private void moveGeneratedArtifactsToArtifactsDirectory() {
        def codeSign = extension.build.signingIdentity.name
        def configuration = project.xcodebuild.configuration
        def sdk = extension.build.sdk
        def productName = project.xcodebuild.productName

        def artifactsDir = getArtifactsDir()
        artifactsDir.mkdirs()

        if (sdk == 'iphoneos') {
            // Zip file with the debug symbols
            project.ant.zip(destfile: new File(artifactsDir, artifactFileName(productName, sdk, codeSign, 'zip')).toString()) {
                zipfileset(prefix: "${productName}.app.dSYM", dir: "${targetOutputDir(codeSign)}/${configuration}-${sdk}/${productName}.app.dSYM")
            }

            FileUtils.copyFile(new File(project.buildDir, "package/${productName}.ipa"), new File(artifactsDir, artifactFileName(productName, sdk, codeSign, 'ipa')))
        } else {
            FileUtils.copyFile(new File(project.buildDir, "archive/${productName}.zip"), new File(artifactsDir, artifactFileName(productName, sdk, codeSign, 'zip')))
        }
    }

    private File getArtifactsDir() {
        new File(project.buildDir, 'artifacts')
    }

    private File fullPathToArtifact(String identityName, String sdk, String type) {
        new File(getArtifactsDir(), artifactFileName(extension.build.productName, sdk, identityName, type))
    }

    private static String artifactFileName(String productName, String sdk, String codeSign, String extension) {
        "${productName}-${sdk}-${codeSign}.${extension}"
    }

    private String pathToRootPlist() {
        def codeSign = extension.build.signingIdentity.name
        def configuration = extension.build.configuration
        def sdk = extension.build.sdk
        def appName = extension.build.productName

        "${targetOutputDir(codeSign)}/${configuration}-${sdk}/${appName}.app/Settings.bundle/Root.plist"
    }
}
