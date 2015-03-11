package com.gap.gradle.plugins.iossigning

import com.gap.gradle.plugins.airwatch.util.CommandRunner

class Security {
    private static final String SECURITY_TOOL = "/usr/bin/security"

    private final CommandRunner commandRunner

    public Security(CommandRunner commandRunner) {
        this.commandRunner = commandRunner
    }

    public void createKeychain(String password, File keychain) {
        commandRunner.run(SECURITY_TOOL, "create-keychain", "-p", password, keychain.absolutePath)
    }

    public void importCertificate(File certificate, File keychain, String certificatePassword, String allowedAppPath) {
        commandRunner.run(SECURITY_TOOL, "-v", "import", certificate, "-k", keychain.absolutePath, "-P", certificatePassword, "-T", allowedAppPath)
    }

    public void deleteKeychain(File keychain) {
        commandRunner.run(SECURITY_TOOL, "delete-keychain", keychain.absolutePath)
    }

    public void addKeychain(File keychain) {
        def currentKeychains = getKeychainList()
        setKeychainList(currentKeychains << keychain.absolutePath)
    }

    public String decodeCMSMessages(File infile) {
        commandRunner.run(SECURITY_TOOL, "cms", "-D", "-i", infile.absolutePath)
    }

    private Collection<String> getKeychainList() {
        String keychainList = commandRunner.run(SECURITY_TOOL, "list-keychains")
        def cleanedKeychainPaths = keychainList.split("\n").collect { it.replaceAll(/^\s*\"|\"$/, "") }
        return cleanedKeychainPaths;
    }

    private void setKeychainList(Collection<String> keychainList) {
        def commandList = [SECURITY_TOOL, "list-keychains", "-s" ]

        def existingKeychains = keychainList.findAll { new File(it).exists() }
        commandList.addAll(existingKeychains)

        commandRunner.run(commandList.toArray(new Object[commandList.size()]))
    }
}
