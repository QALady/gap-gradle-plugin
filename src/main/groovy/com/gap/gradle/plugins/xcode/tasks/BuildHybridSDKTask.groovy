package com.gap.gradle.plugins.xcode.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import com.gap.gradle.plugins.mobile.CommandRunner
import com.gap.gradle.plugins.iossigning.Zipper
import org.apache.commons.io.FileUtils

class BuildHybridSDKTask extends DefaultTask {

    String sdkScheme
    
    private String targetDir
    private Zipper zipper
    
    @TaskAction
    def generateHybridSDK() {
        buildHybridSDK()
    }

    private buildHybridSDK() {
        
        def commandRunner = new CommandRunner(project)

        project.copy {
            from "${project.buildDir}/${sdkScheme}/Release-iphoneos/${sdkScheme}.framework/"
            into "${project.buildDir}/DerivedData-universal/${sdkScheme}.framework/"
        }

        commandRunner.run("lipo", "-create", "-output", "${project.buildDir}/DerivedData-universal/${sdkScheme}.framework/${sdkScheme}", "${project.buildDir}/${sdkScheme}/Release-iphonesimulator/${sdkScheme}.framework/${sdkScheme}", "${project.buildDir}/${sdkScheme}/Release-iphoneos/${sdkScheme}.framework/${sdkScheme}")

        zipper = new Zipper(commandRunner)


        def hybridSDKDir = new File(project.buildDir, "DerivedData-universal")

        def hydridSDKFile = new File(hybridSDKDir, "${sdkScheme}.framework.zip")
        hydridSDKFile.delete()

        zipper.zip(hybridSDKDir, hydridSDKFile)

        project.configurations {
            hybridSDK
        }
        project.artifacts {
            hybridSDK hydridSDKFile
        }
/*
        project.uploadHybridSDK {
            repositories {
                ivy {
                    layout "maven"
                    url project.ivy.url
                    credentials {
                        username project.ivy.userName
                        password project.ivy.password
                    }
                }
            }
        }

        project.uploadHybridSDK.repositories {
            add project.repositories.wm_local_non_prod
        }
        project.uploadHybridSDK.execute() */
    }

}
