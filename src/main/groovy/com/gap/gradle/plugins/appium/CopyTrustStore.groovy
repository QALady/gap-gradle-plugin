package com.gap.gradle.plugins.appium

import org.apache.tools.ant.DirectoryScanner
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static java.lang.System.getProperty

class CopyTrustStore extends DefaultTask {
    private static final String IOS7_KEYCHAIN_BASE_DIR = "${getProperty('user.home')}/Library/Application Support/iPhone Simulator"
    private static final String IOS8_KEYCHAIN_BASE_DIR = "${getProperty('user.home')}/Library/Developer/CoreSimulator/Devices"

    String trustStoreFile

    @TaskAction
    run() {
        keychainPathsFor(IOS7_KEYCHAIN_BASE_DIR, IOS8_KEYCHAIN_BASE_DIR).each { File destination ->
            project.logger.info "Copying \"${trustStoreFile}\" into \"$destination\""

            project.copy {
                from trustStoreFile
                into destination
            }
        }
    }

    public List keychainPathsFor(String... baseDir) {
        def result = []

        DirectoryScanner scanner = new DirectoryScanner()
        scanner.setCaseSensitive(false)
        scanner.setIncludes("**/Keychains")

        baseDir.each { dir ->
            scanner.setBasedir(dir)
            scanner.scan()
            scanner.getIncludedDirectories().each {
                result.add(new File(scanner.getBasedir(), it))
            }
        }

        result
    }
}
