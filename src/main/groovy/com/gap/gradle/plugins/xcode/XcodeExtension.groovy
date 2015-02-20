package com.gap.gradle.plugins.xcode

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator

import static org.apache.commons.lang.StringUtils.isBlank

class XcodeExtension {

    private Property<String> workspace
    private Property<String> scheme

    XcodeTestConfig test
    XcodeBuildConfig build
    XcodeArchiveConfig archive
    final NamedDomainObjectSet<SigningIdentity> signing

    XcodeExtension(Instantiator instantiator, Project project) {
        this.test = instantiator.newInstance(XcodeTestConfig, instantiator, this)
        this.build = instantiator.newInstance(XcodeBuildConfig, this)
        this.archive = instantiator.newInstance(XcodeArchiveConfig)
        this.signing = project.container(SigningIdentity, { name -> instantiator.newInstance(SigningIdentity, name) })
    }

    void test(Action<XcodeTestConfig> action) {
        action.execute(test)
    }

    void build(Action<XcodeBuildConfig> action) {
        action.execute(build)
    }

    void archive(Action<XcodeArchiveConfig> action) {
        action.execute(archive)
    }

    void signing(Action<? super NamedDomainObjectCollection<SigningIdentity>> action) {
        action.execute(signing)
    }

    String getWorkspace() {
        return workspace?.get()
    }

    void setWorkspace(Object workspace) {
        this.workspace = new Property(workspace)
    }

    String getScheme() {
        return scheme?.get()
    }

    void setScheme(Object scheme) {
        this.scheme = new Property(scheme)
    }

    boolean isTargetRequired() {
        return isBlank(getScheme())
    }
}
