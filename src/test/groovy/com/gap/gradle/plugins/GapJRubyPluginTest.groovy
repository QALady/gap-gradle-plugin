package com.gap.gradle.plugins
import static org.hamcrest.CoreMatchers.notNullValue
import static org.junit.Assert.assertThat

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class GapJRubyPluginTest {

    @Test
    public void shouldAddJRubyConfiguration(){
        def project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapjruby'
        assertThat(project.configurations.getByName('jruby'), notNullValue())
    }

}
