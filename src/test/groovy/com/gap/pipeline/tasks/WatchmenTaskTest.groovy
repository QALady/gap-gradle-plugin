package com.gap.pipeline.tasks

import com.gap.pipeline.exception.MissingParameterException
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

import static org.junit.rules.ExpectedException.none

@RequiredParameters([
@Require(parameter = 'ivy.url', description = ""),
@Require(parameter = 'location', description = ""),
@Require(parameter = 'chef.jenkins.url', description = "")
])
class TestClassWithAnnotations extends WatchmenTask {
    TestClassWithAnnotations(project){
        super(project)
    }
}

class TestClassWithNoAnnotations extends WatchmenTask {
    TestClassWithNoAnnotations(project){
        super(project)
    }
}

class WatchmenTaskTest {
    def project

    @Rule
    public ExpectedException exception = none()

    @Before
    void setUp(){
        project = ProjectBuilder.builder().build()
    }

    @Test
    void shouldThrowException_whenMissingParametersWithoutDotNotation () {
        exception.expect(MissingParameterException)
        exception.expectMessage("Missing required parameter: 'location'")
        project.ext['ivy.url'] = "test"
        project.ext['chef.jenkins.url'] = "url"
        new TestClassWithAnnotations(project).validate()
    }

    @Test
    void shouldThrowException_whenParameterValueIsNull () {
        exception.expect(MissingParameterException)
        exception.expectMessage("Missing required parameter: 'chef.jenkins.url'")
        project.ext.location = "location"
        project.ext['ivy.url'] = "test"
        project.ext['chef.jenkins.url'] = null
        new TestClassWithAnnotations(project).validate()
    }

    @Test
    void shouldThrowException_whenMissingParametersWithDotNotation () {
        exception.expect(MissingParameterException)
        exception.expectMessage("Missing required parameter: 'chef.jenkins.url'")
        project.ext.location = "test"
        project.ext['ivy.url'] = "url"
        new TestClassWithAnnotations(project).validate()
    }

    @Test
    void shouldSuccessfullyValidate_whenAllParametersArePresent () {
        project.ext.location = "test"
        project.ext['ivy.url'] = "url"
        project.ext['chef.jenkins.url'] = "url"
        new TestClassWithAnnotations(project).validate()
    }



    @Test
    void shouldSuccessfullyValidateExtensionParameters () {
        project.ext.location = "test"
        project.apply plugin: 'gappipeline'
        project.ivy.url = "url"
        project.ext['chef.jenkins.url'] = "url"
        new TestClassWithAnnotations(project).validate()
    }

    @Test
    void shouldSuccessfullyValidate_whenClassHasNoAnnotations(){
        new TestClassWithNoAnnotations(project).validate()
    }
}

