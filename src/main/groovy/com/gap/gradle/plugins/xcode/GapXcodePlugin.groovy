package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.tasks.TransformJUnitXmlReportToHTMLTask
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

        configureExistingTasks()
        createNewTasks()
        configureTaskGraph()
    }

    private configureExistingTasks() {
        project.tasks.findByName('test').finalizedBy('transformJUnitXmlReportToHTML')

        project.tasks.findByName('codesign').doFirst {
            def codeSign = extension.build.signingIdentity.name
            def configuration = project.xcodebuild.configuration
            def sdk = extension.build.sdk

            def filePath = "${targetOutputDir(codeSign)}/${configuration}-${sdk}"
            def fileTree = project.fileTree(filePath)
            def rootPlistPath = fileTree.include('**/Root.plist').asPath

            ant.replace(file: rootPlistPath, token: '@version@', value: extension.archive.version)
        }

        project.tasks.findByName('uploadArchives').dependsOn('airwatchConfigZip')
    }

    private createNewTasks() {
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

            if (taskGraph.hasTask(':build')) {
                extension.build.validate()
                def buildConfig = extension.build
                def signIdentity = buildConfig.signingIdentity

                project.xcodebuild {
                    target = buildConfig.target
                    sdk = buildConfig.sdk
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

                project.xcodebuild.configuration = 'Release'
            }

            if (taskGraph.hasTask(':uploadArchives')) {
                extension.archive.validate()
                project.version = extension.archive.version

                project.artifacts {
                    def chosenIdentity = extension.build.signingIdentity.name
                    archives classifier: 'iphoneos', file: fullPathToArtifact(chosenIdentity, 'iphoneos', 'ipa')
                    archives classifier: 'iphoneos', file: fullPathToArtifact(chosenIdentity, 'iphoneos', 'zip')
                    archives classifier: 'iphonesimulator', file: fullPathToArtifact(chosenIdentity, 'iphonesimulator', 'zip')

                    def developmentIdentity = extension.signing.development.name
                    archives classifier: 'iphoneos-dev', file: fullPathToArtifact(developmentIdentity, 'iphoneos', 'ipa')
                    archives classifier: 'iphoneos-dev', file: fullPathToArtifact(developmentIdentity, 'iphoneos', 'zip')
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
        def appName = extension.build.target
        def basePath = project.buildDir

        project.file("${basePath}/sym-${identityName}/${appName}")
    }

    private File fullPathToArtifact(identityName, sdk, type) {
        def basePath = targetOutputDir(identityName).path
        def appName = extension.build.target
        def fileName = "${appName}.${type}"

        project.file("${basePath}/Release-${sdk}/${fileName}")
    }
}
