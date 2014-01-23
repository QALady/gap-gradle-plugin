package com.gap.gradle.chef

import static net.sf.ezmorph.test.ArrayAssertions.assertEquals

import com.gap.gradle.utils.ShellCommand
import groovy.mock.interceptor.MockFor
import org.junit.Ignore
import org.junit.Test

class CookbookMetadataTest {

    @Test
    void shouldRunKnifeCommandToGenerateMetadataJson() {
        def mockShellCommand = new MockFor(ShellCommand)
        mockShellCommand.demand.execute { command ->
            assertEquals("knife cookbook metadata from file /home/cookbookdir/metadata.rb", command)
        }
        mockShellCommand.use {
            new CookbookMetadata().loadFrom("/home/cookbookdir")
        }
    }

    @Ignore
    void shouldLoadCookbookNameAndVersionFromMetadata(){
        def mockShellCommand = new MockFor(ShellCommand)
        mockShellCommand.ignore.execute{}
        mockShellCommand.use {
            new CookbookMetadata().loadFrom("/home/cookbookdir")
        }
    }


}
