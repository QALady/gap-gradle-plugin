package com.gap.gradle.ivy

import org.junit.Test
import org.gradle.testfixtures.ProjectBuilder

import static org.junit.Assert.assertThat
import static org.hamcrest.CoreMatchers.is
import static matchers.CustomMatchers.sameString

class IvyInfoTest {

    @Test
    public void identifiers_shouldReturnIdentifierOfGivenProject(){
        def project = ProjectBuilder.builder().withName('moduleName').build()
        project.group = 'com.gap.ivyGroup'
        def ivyInfo = new IvyInfo(project)
        assertThat(ivyInfo.identifiers(), is([sameString('com.gap.ivyGroup:moduleName')] as Set))
    }

    @Test
    public void identifiers_shouldIncludeIndentifiersOfSubmodules(){
        def project = ProjectBuilder.builder().withName('moduleName').build()
        project.group = 'com.gap.ivyGroup'
        def dependentModule = ProjectBuilder.builder().withParent(project).withName('anotherApp').build()
        dependentModule.group = 'com.gap.watchmen'
        def ivyInfo = new IvyInfo(project)
        assertThat(ivyInfo.identifiers(), is([sameString('com.gap.ivyGroup:moduleName'), sameString('com.gap.watchmen:anotherApp')] as Set))
    }

    @Test
    public void dependencies_shouldReturnIdentifiersOfProjectDependencies(){
        def project = ProjectBuilder.builder().build()
        project.configurations.add('configurationName')
        project.dependencies.add('configurationName', 'com.external.lib:superGreatLibrary:1.1.3')
        project.dependencies.add('configurationName', 'com.gap.otherTeam:internalLib:+')
        def ivyInfo = new IvyInfo(project)
        def expectedDependencies = [sameString("com.external.lib:superGreatLibrary"), sameString("com.gap.otherTeam:internalLib")] as Set
        assertThat(ivyInfo.dependencies(), is(expectedDependencies))
    }

    @Test
    public void dependencies_shouldIncludeDependenciesOfSubModules(){
        def project = ProjectBuilder.builder().build()
        project.configurations.add('configurationName')
        project.dependencies.add('configurationName', 'com.external.lib:superGreatLibrary:1.1.3')
        project.dependencies.add('configurationName', 'com.gap.otherTeam:internalLib:+')


        def subProject = ProjectBuilder.builder().withParent(project).withName('anotherApp').build()
        subProject.group = 'com.gap.watchmen'
        subProject.configurations.add('test')
        subProject.dependencies.add('test', 'org.junit:junit:+')



        def ivyInfo = new IvyInfo(project)

        def expectedDependencies = [sameString("com.external.lib:superGreatLibrary"), sameString("com.gap.otherTeam:internalLib"), sameString("org.junit:junit")] as Set

        assertThat(ivyInfo.dependencies(), is(expectedDependencies))
    }

    @Test
    public void dependencies_shouldNotHaveDuplicateDependencies(){
        def project = ProjectBuilder.builder().build()
        project.configurations.add('configurationName')
        project.dependencies.add('configurationName', 'com.external.lib:superGreatLibrary:1.1.3')
        project.dependencies.add('configurationName', 'com.gap.otherTeam:internalLib:+')
        def subProject = ProjectBuilder.builder().withParent(project)withName('anotherApp').build()
        subProject.group = 'com.gap.watchmen'
        subProject.configurations.add('test')
        subProject.dependencies.add('test', 'com.gap.otherTeam:internalLib:+')

        def ivyInfo = new IvyInfo(project)

        def expectedDependencies = [sameString("com.external.lib:superGreatLibrary"), sameString("com.gap.otherTeam:internalLib")] as Set

        assertThat(ivyInfo.dependencies(), is(expectedDependencies))
    }

    @Test
    public void version_shouldReturnVersionSegment(){
        def project = ProjectBuilder.builder().build()
        project.version = '1234'
        def ivyInfo = new IvyInfo(project)
        assertThat(ivyInfo.version(), is('1234'))
    }

}
