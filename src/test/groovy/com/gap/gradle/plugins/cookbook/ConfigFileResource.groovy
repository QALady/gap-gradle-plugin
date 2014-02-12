package com.gap.gradle.plugins.cookbook

import org.junit.rules.ExternalResource
import org.junit.rules.TemporaryFolder

class ConfigFileResource extends ExternalResource {

    final def temp
    final def clazz
    final def property
    private def originalValue

    def ConfigFileResource(clazz, property) {
        this.clazz = clazz
        this.property = property
        this.temp = new TemporaryFolder()
    }

    @Override
    protected void before() throws Throwable {
        super.before()
        temp.before()
        originalValue = clazz."${property}"
        clazz."${property}" = temp.newFile("temp").absolutePath
    }

    @Override
    protected void after() {
        super.after()
        temp.after()
        clazz."${property}" = originalValue
    }
}
