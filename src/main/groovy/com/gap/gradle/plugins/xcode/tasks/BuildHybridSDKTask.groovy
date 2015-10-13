package com.gap.gradle.plugins.xcode.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.bundling.Zip
import com.gap.gradle.plugins.mobile.CommandRunner

class BuildHybridSDKTask extends DefaultTask {

    @Input
    String sdkScheme
    
    @TaskAction
    def generateHybridSDK() {
        buildHybridSDK()
    }

    private buildHybridSDK() {

        def commandRunner = new CommandRunner(project)
        
        def hybridSDKDir = new File(project.buildDir, "DerivedData-universal")

        def hydridSDKFile = new File(hybridSDKDir, "${sdkScheme}.framework.zip")

        def dSYMFile =  new File(hybridSDKDir,"${sdkScheme}.framework.dSYM.zip")

        hydridSDKFile.delete()
        dSYMFile.delete()
        project.copy {
            from "${project.buildDir}/${sdkScheme}/Release-iphoneos/"
            include "${sdkScheme}.framework/**"
            include "${sdkScheme}.framework.dSYM/**"

        into "${project.buildDir}/DerivedData-universal/" 
    }


        commandRunner.run("lipo", "-create", "-output", "${project.buildDir}/DerivedData-universal/${sdkScheme}.framework/${sdkScheme}", "${project.buildDir}/${sdkScheme}/Release-iphonesimulator/${sdkScheme}.framework/${sdkScheme}", "${project.buildDir}/${sdkScheme}/Release-iphoneos/${sdkScheme}.framework/${sdkScheme}")


        project.ant.zip(destfile: hydridSDKFile.toString()) {
            zipfileset(prefix: "${sdkScheme}.framework", dir: "${hybridSDKDir}/${sdkScheme}.framework")
        }

        project.ant.zip(destfile: dSYMFile.toString()) {
            zipfileset(prefix: "${sdkScheme}.framework.dSYM", dir: "${hybridSDKDir}/${sdkScheme}.framework.dSYM")
        }

        project.configurations {
            hybridSDK
        }
        project.artifacts {
            hybridSDK hydridSDKFile
            hybridSDK dSYMFile
        }
    }

}
