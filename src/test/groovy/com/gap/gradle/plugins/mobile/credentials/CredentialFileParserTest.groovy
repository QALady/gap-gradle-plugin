package com.gap.gradle.plugins.mobile.credentials

import org.apache.commons.io.FilenameUtils
import org.gradle.api.GradleException
import org.junit.Before
import org.junit.Test

import static java.io.File.createTempFile
import static org.hamcrest.CoreMatchers.containsString
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail
import static org.testng.Assert.assertEquals

public class CredentialFileParserTest {

    def CredentialFileParser credentialFileParser
    def File workingDir

    @Before
    public void setUp() throws Exception {
        workingDir = new File(System.getProperty("java.io.tmpdir"))
        credentialFileParser = new CredentialFileParser(workingDir)
    }

    @Test
    public void shouldReturnCredential() throws Exception {
        def fakeCredentialFile = createTempFile("fake", ".credential", workingDir)
        fakeCredentialFile << """username: foo
password: bar
"""

        def credentialName = fileNameWithoutExtension(fakeCredentialFile)

        Credential credential = credentialFileParser.parse(credentialName)
        assertEquals(credential.username, "foo")
        assertEquals(credential.password, "bar")
    }

    @Test
    public void shouldFailWhenFormatIsIncorrect() throws Exception {
        def fakeCredentialFile = createTempFile("fake", ".credential", workingDir)
        fakeCredentialFile << "invalid format"

        def credentialName = fileNameWithoutExtension(fakeCredentialFile)

        try {
            credentialFileParser.parse(credentialName)

            fail("Expected exception not thrown")
        } catch (GradleException e) {
            assertThat(e.message, containsString(CredentialFileParser.CREDENTIAL_PATTERN))
        }
    }

    private String fileNameWithoutExtension(File fakeCredentialFile) {
        FilenameUtils.removeExtension(fakeCredentialFile.name)
    }
}
