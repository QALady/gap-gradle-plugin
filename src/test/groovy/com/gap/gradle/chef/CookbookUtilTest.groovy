package com.gap.gradle.chef

import com.gap.gradle.utils.ShellCommand
import groovy.mock.interceptor.MockFor
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static junit.framework.Assert.assertFalse
import static junit.framework.Assert.assertTrue
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals

class CookbookUtilTest {

    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    def cookbookUtil
    def mockShellCommand

    @Before
    void setUp() {
        cookbookUtil = new CookbookUtil()
        mockShellCommand = new MockFor(ShellCommand)
    }

    @Test
    void metadataFrom_shouldRunKnifeCommandToGenerateMetadataJson() {
        temp.newFile("metadata.json").write("{}")
        mockShellCommand.demand.execute { command ->
            assertEquals("knife cookbook metadata from file ${temp.root.path}/metadata.rb", command)
        }
        mockShellCommand.use {
            cookbookUtil.metadataFrom(temp.root.path)
        }
    }

    @Test
    void metadataFrom_shouldLoadCookbookNameAndVersionFromMetadata(){
        mockShellCommand.ignore.execute { command -> }

        File metadataFile = temp.newFile("metadata.json")
        metadataFile.write('{"name": "mycookbook", "version": "1.1.13"}')

        mockShellCommand.use {
            def cookbookMetadata = cookbookUtil.metadataFrom(metadataFile.getParent())
            assertEquals("mycookbook", cookbookMetadata.name)
            assertEquals("1.1.13", cookbookMetadata.version)
        }
    }

    @Test
    void doesCookbookExist_shouldReturnTrue_whenCookbookFound(){
        mockShellCommand.demand.execute{ command ->
            assertEquals("knife cookbook show myapp", command)
            return "myapp   1.1.13  0.0.41  0.0.39"
        }
        mockShellCommand.use {
            assertTrue(new CookbookUtil().doesCookbookExist([name: 'myapp', version: '1.1.13']))
        }
    }

    @Test
    void doesCookbookExist_shouldReturnFalse_whenCookbookVersionCannotBeFound(){
        mockShellCommand.demand.execute{ command ->
            assertEquals("knife cookbook show myapp", command)
            return "myapp   1.1.12  0.0.41  0.0.39"
        }
        mockShellCommand.use {
            assertFalse(new CookbookUtil().doesCookbookExist([name: 'myapp', version: '1.1.13']))
        }
    }

    @Test
    void doesCookbookExist_shouldReturnFalse_whenTheShellCommandThrowsAnException(){
        mockShellCommand.demand.execute{ command, path ->
            throw new Exception("Command failed")
        }
        mockShellCommand.use {
            assertFalse(new CookbookUtil().doesCookbookExist([name: 'myapp', version: '1.1.13']))
        }
    }
}
