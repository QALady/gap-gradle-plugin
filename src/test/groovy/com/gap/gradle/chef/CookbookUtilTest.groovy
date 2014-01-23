package com.gap.gradle.chef
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals

import com.gap.gradle.utils.ShellCommand
import groovy.mock.interceptor.MockFor
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CookbookUtilTest {

    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    @Test
    void shouldRunKnifeCommandToGenerateMetadataJson() {
        def mockShellCommand = new MockFor(ShellCommand)
        temp.newFile("metadata.json").write("{}")
        mockShellCommand.demand.execute { command ->
            assertEquals("knife cookbook metadata from file ${temp.root.path}/metadata.rb", command)
        }
        mockShellCommand.use {
            CookbookUtil.metadataFrom(temp.root.path)
        }
    }

    @Test
    void shouldLoadCookbookNameAndVersionFromMetadata(){
        def mockShellCommand = new MockFor(ShellCommand)
        mockShellCommand.ignore.execute {}

        File metadataFile = temp.newFile("metadata.json")
        metadataFile.write('{"name": "mycookbook", "version": "1.1.13"}')

        mockShellCommand.use {
            def cookbookMetadata = CookbookUtil.metadataFrom(metadataFile.getParent())
            assertEquals("mycookbook", cookbookMetadata.name)
            assertEquals("1.1.13", cookbookMetadata.version)
        }
    }
}
