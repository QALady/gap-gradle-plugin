package com.gap.pipeline.ec

import org.gradle.testfixtures.ProjectBuilder
import com.gap.gradle.ivy.IvyInfo

import static org.mockito.Mockito.when
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.mock
import static matchers.CustomMatchers.sameString
import org.junit.Test
import org.junit.Before
import org.gradle.api.Project

class SegmentRegistryTest {


    Project project
    CommanderClient commander

    @Before
    public void setUp(){
        project = ProjectBuilder.builder().withName('moduleName').build()
        commander = mock(CommanderClient)
        def segmentConfig = new SegmentConfig('http://svn.gap.dev/path/to/repo', '/dev/shm/1234/job_id', '/mnt/electric-commander/workspace/job_id', 'build.gradle')
        when(commander.getSegmentConfig()).thenReturn(segmentConfig)
        when(commander.getECProperty('/myJob/projectName')).thenReturn("Project Name")
        when(commander.getECProperty('/myJob/liveProcedure')).thenReturn("Procedure Name")
        project.group = 'com.gap.watchmen'
    }

    @Test
    public void populate_shouldSetIvyIdentifiersForCurrentSegment(){
        def ivyInfo = new IvyInfo(project)
        def registry = new SegmentRegistry(commander)

        registry.populate(ivyInfo)

        verify(commander).setECProperty(sameString("/Projects[WM Segment Registry]/SegmentRegistry/Project Name:Procedure Name/ivyIdentifiers"), 'com.gap.watchmen:moduleName')
    }

    @Test
    public void populate_shouldSetIdentifiersCorrectlyForAMultiModuleBuild(){
        def subModule = ProjectBuilder.builder().withParent(project).withName('anotherApp').build()
        subModule.group = 'com.gap.watchmen'
        def ivyInfo = new IvyInfo(project)
        def registry = new SegmentRegistry(commander)

        registry.populate(ivyInfo)

        verify(commander).setECProperty(sameString("/Projects[WM Segment Registry]/SegmentRegistry/Project Name:Procedure Name/ivyIdentifiers"), 'com.gap.watchmen:moduleName\ncom.gap.watchmen:anotherApp')
    }

    @Test
    public void populate_shouldSetDependenciesForCurrentSegment(){
        project.configurations.add('configName')
        project.dependencies.add('configName', 'com.gap.coolTeam:theirLib:1.+')
        def subModule = ProjectBuilder.builder().withParent(project).withName('anotherApp').build()
        subModule.group = 'com.gap.watchmen'
        subModule.configurations.add('test')
        subModule.dependencies.add('test', 'com.junit:junit:+')
        def ivyInfo = new IvyInfo(project)
        def registry = new SegmentRegistry(commander)

        registry.populate(ivyInfo)

        verify(commander).setECProperty(sameString("/Projects[WM Segment Registry]/SegmentRegistry/Project Name:Procedure Name/ivyDependencies"), 'com.gap.coolTeam:theirLib\ncom.junit:junit')
    }

    @Test
    public void populate_shouldSetSegmentConfigValues(){
        def ivyInfo = new IvyInfo(project)
        def registry = new SegmentRegistry(commander)
        registry.populate(ivyInfo)

        verify(commander).setECProperty(sameString("/Projects[WM Segment Registry]/SegmentRegistry/Project Name:Procedure Name/svnUrl"), 'http://svn.gap.dev/path/to/repo')
        verify(commander).setECProperty(sameString("/Projects[WM Segment Registry]/SegmentRegistry/Project Name:Procedure Name/workingDir"), '/dev/shm/1234/job_id')
        verify(commander).setECProperty(sameString("/Projects[WM Segment Registry]/SegmentRegistry/Project Name:Procedure Name/ciDir"), '/mnt/electric-commander/workspace/job_id')
        verify(commander).setECProperty(sameString("/Projects[WM Segment Registry]/SegmentRegistry/Project Name:Procedure Name/gradleFile"), 'build.gradle')
    }



}
