package com.gap.pipeline.ec

import org.junit.Ignore

import static matchers.CustomMatchers.sameString
import static org.junit.rules.ExpectedException.none
import static org.mockito.Matchers.anyString
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.*

import com.gap.gradle.ivy.IvyInfo
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class SegmentRegistryTest {


    Project project
    CommanderClient commander
    def projectName = "Project Name"
    def procedureName = "Procedure Name"

    @Rule
    public final ExpectedException exception = none()

    @Before
    public void setUp(){
        project = ProjectBuilder.builder().withName('moduleName').build()
        project.group = 'com.gap.watchmen'

        def segment = new Segment(projectName, procedureName)
        def segmentConfig = new SegmentConfig('http://svn.gap.dev/path/to/repo', '/dev/shm/1234/job_id', 'build.gradle', 'ci svn repo', false)
        commander = mock(CommanderClient)
        when(commander.getCurrentSegmentConfig()).thenReturn(segmentConfig)
        when(commander.getCurrentSegment()).thenReturn(segment)
    }

    @Test
    public void populate_shouldSetSegmentConfigValues(){
        def ivyInfo = new IvyInfo(project)
        def registry = new SegmentRegistry(commander)

        registry.populate(ivyInfo)

        verify(commander).setECProperty(sameString("/projects[WM Segment Registry]/SegmentRegistry/Project Name:Procedure Name/svnUrl"), 'http://svn.gap.dev/path/to/repo')
        verify(commander).setECProperty(sameString("/projects[WM Segment Registry]/SegmentRegistry/Project Name:Procedure Name/workingDir"), '/dev/shm/1234/job_id')
        verify(commander).setECProperty(sameString("/projects[WM Segment Registry]/SegmentRegistry/Project Name:Procedure Name/gradleFile"), 'build.gradle')
        verify(commander).setECProperty(sameString("/projects[WM Segment Registry]/SegmentRegistry/Project Name:Procedure Name/scmConfigName"), 'ci svn repo')
        verify(commander).setECProperty(sameString("/projects[WM Segment Registry]/SegmentRegistry/Project Name:Procedure Name/isManual"), 'false')
    }

    @Test
    public void populate_shouldSetIvyIdentifiersForCurrentSegment(){
        def ivyInfo = new IvyInfo(project)
        def registry = new SegmentRegistry(commander)

        registry.populate(ivyInfo)

        verify(commander).setECProperty(sameString("/projects[WM Segment Registry]/SegmentRegistry/Project Name:Procedure Name/ivyIdentifiers"), 'com.gap.watchmen:moduleName')
    }

    @Test
    public void populate_shouldSetIdentifiersCorrectlyForAMultiModuleBuild(){
        def subModule = ProjectBuilder.builder().withParent(project).withName('anotherApp').build()
        subModule.group = 'com.gap.watchmen'
        def ivyInfo = new IvyInfo(project)
        def registry = new SegmentRegistry(commander)

        registry.populate(ivyInfo)

        verify(commander).setECProperty(sameString("/projects[WM Segment Registry]/SegmentRegistry/Project Name:Procedure Name/ivyIdentifiers"), 'com.gap.watchmen:moduleName\ncom.gap.watchmen:anotherApp')
    }

    @Test
    public void populate_shouldSetDependenciesForCurrentSegment(){
        project.configurations.create('configName')
        project.dependencies.add('configName', 'com.gap.coolTeam:theirLib:1.+')
        def subModule = ProjectBuilder.builder().withParent(project).withName('anotherApp').build()
        subModule.group = 'com.gap.watchmen'
        subModule.configurations.create('test')
        subModule.dependencies.add('test', 'com.junit:junit:+')
        def ivyInfo = new IvyInfo(project)
        def registry = new SegmentRegistry(commander)

        registry.populate(ivyInfo)

        verify(commander).setECProperty(sameString("/projects[WM Segment Registry]/SegmentRegistry/Project Name:Procedure Name/ivyDependencies"), 'com.gap.coolTeam:theirLib\ncom.junit:junit')
    }

    @Test
    public void registerWithUpstreamSegments_shouldRegisterSelfWithUpstreamSegment(){
        addUpstreamDependency("org.gap.team:component", "Some Team's Project:Component Segment", project)
        arrangePriorUpstreamSegments("${projectName}:${procedureName}", "")
        arrangePriorDownstreamSegments("Some Team's Project:Component Segment", "")
        def ivyInfo = new IvyInfo(project)
        def registry = new SegmentRegistry(commander)

        registry.registerWithUpstreamSegments(ivyInfo)

        verify(commander).setECProperty(sameString("/projects[WM Segment Registry]/SegmentRegistry/Some Team's Project:Component Segment/downstreamSegments"), 'Project Name:Procedure Name')
    }

    //@Test
    public void registerWithUpstreamSegments_shouldNotOverwriteOtherDownstreamSegments(){
        addUpstreamDependency("org.gap.team:component", "Some Team's Project:Component Segment", project)
        arrangePriorUpstreamSegments("${projectName}:${procedureName}", "")
        arrangePriorDownstreamSegments("Some Team's Project:Component Segment", "Another Project:Downstream Segment")
        def ivyInfo = new IvyInfo(project)
        def registry = new SegmentRegistry(commander)

        registry.registerWithUpstreamSegments(ivyInfo)

        verify(commander).setECProperty(sameString("/projects[WM Segment Registry]/SegmentRegistry/Some Team's Project:Component Segment/downstreamSegments"), 'Another Project:Downstream Segment\nProject Name:Procedure Name')
    }

    @Test
    public void registerWithUpstreamSegments_shouldDoNothingIfDependenciesIsNotInIdentifierRegistry(){
        project.configurations.create('configName')
        project.dependencies.add('configName', 'org.gap.team:component:+')
        when(commander.getECProperty(sameString('/projects[WM Segment Registry]/IdentifierRegistry/org.gap.team:component/segment'))).thenReturn(Property.invalidProperty("/projects[WM Segment Registry]/IdentifierRegistry/org.gap.team:component/segment"))
        arrangePriorUpstreamSegments("${projectName}:${procedureName}", "")
        def ivyInfo = new IvyInfo(project)
        def registry = new SegmentRegistry(commander)

        registry.registerWithUpstreamSegments(ivyInfo)

        verify(commander, never()).setECProperty(eq("/projects[WM Segment Registry]/SegmentRegistry/Some Team's Project:Component Segment/downstreamSegments"), anyString())

    }

    @Test
    public void registerWithUpstreamSegments_shouldNotAddDuplicateEntries(){
        addUpstreamDependency("org.gap.team:component", "Some Team's Project:Component Segment", project)
        arrangePriorUpstreamSegments("${projectName}:${procedureName}", "")
        arrangePriorDownstreamSegments("Some Team's Project:Component Segment", "Project Name:Procedure Name")
        def ivyInfo = new IvyInfo(project)
        def registry = new SegmentRegistry(commander)

        registry.registerWithUpstreamSegments(ivyInfo)

        verify(commander).setECProperty(sameString("/projects[WM Segment Registry]/SegmentRegistry/Some Team's Project:Component Segment/downstreamSegments"), 'Project Name:Procedure Name')
    }

    @Test
    public void registerWithUpstreamSegments_shouldNotRegisterWithSelf(){
        addUpstreamDependency("org.gap.team:component", "${projectName}:${procedureName}".toString(), project)
        arrangePriorUpstreamSegments("${projectName}:${procedureName}", "")
        def ivyInfo = new IvyInfo(project)
        def registry = new SegmentRegistry(commander)

        registry.registerWithUpstreamSegments(ivyInfo)

        verify(commander, never()).setECProperty(eq("/projects[WM Segment Registry]/SegmentRegistry/Some Team's Project:Component Segment/downstreamSegments"), anyString())
    }

    @Test
    public void registerWithUpstreamSegments_shouldAddUpstreamSegmentsToRegistry(){
        addUpstreamDependency("org.gap.team:component", "Some Team's Project:Component Segment", project)
        arrangePriorUpstreamSegments("${projectName}:${procedureName}", "")
        arrangePriorDownstreamSegments("Some Team's Project:Component Segment", "")
        def ivyInfo = new IvyInfo(project)
        def registry = new SegmentRegistry(commander)

        registry.registerWithUpstreamSegments(ivyInfo)

        verify(commander).setECProperty(sameString("/projects[WM Segment Registry]/SegmentRegistry/Project Name:Procedure Name/upstreamSegments"), "Some Team's Project:Component Segment")
    }

    @Test
    public void registerWithUpstreamSegments_shouldUnregisterFromRemovedDependencies(){
        arrangePriorUpstreamSegments("${projectName}:${procedureName}", "Old Project:Deleted Segment")
        arrangePriorDownstreamSegments("Old Project:Deleted Segment", "Another Project:Downstream Segment\n${projectName}:${procedureName}")
        def ivyInfo = new IvyInfo(project)
        def registry = new SegmentRegistry(commander)

        registry.registerWithUpstreamSegments(ivyInfo)

        verify(commander).setECProperty(sameString("/projects[WM Segment Registry]/SegmentRegistry/Old Project:Deleted Segment/downstreamSegments"), "Another Project:Downstream Segment")
    }

    @Ignore
    public void registerWithUpstreamSegments_shouldNotChangeCorrectDependencies(){
        addUpstreamDependency("org.gap.team:component", "Some Team's Project:Component Segment", project)
        arrangePriorUpstreamSegments("${projectName}:${procedureName}", "Some Team's Project:Component Segment")
        arrangePriorDownstreamSegments("Some Team's Project:Component Segment", "Another Project:Downstream Segment\n${projectName}:${procedureName}")
        def ivyInfo = new IvyInfo(project)
        def registry = new SegmentRegistry(commander)

        registry.registerWithUpstreamSegments(ivyInfo)

        verify(commander).setECProperty(sameString("/projects[WM Segment Registry]/SegmentRegistry/Some Team's Project:Component Segment/downstreamSegments"), "Another Project:Downstream Segment\n${projectName}:${procedureName}".toString())
        verify(commander, never()).setECProperty(sameString("/projects[WM Segment Registry]/SegmentRegistry/Some Team's Project:Component Segment/downstreamSegments"), "Another Project:Downstream Segment".toString())
    }

    @Test
    public void populate_shouldCreateEntryForEachIdentifierSegmentProduces(){
        def childProject = ProjectBuilder.builder().withName("anotherIdentifier").withParent(project).build()
        childProject.group = 'com.gap.watchmen'
        def ivyInfo = new IvyInfo(project)
        def registry = new SegmentRegistry(commander)

        registry.populate(ivyInfo)

        verify(commander).setECProperty(sameString("/projects[WM Segment Registry]/IdentifierRegistry/com.gap.watchmen:moduleName/segment"), "${projectName}:${procedureName}".toString())
        verify(commander).setECProperty(sameString("/projects[WM Segment Registry]/IdentifierRegistry/com.gap.watchmen:anotherIdentifier/segment"), "${projectName}:${procedureName}".toString())
    }

    @Test
    public void registerWithUpstreamSegments_shouldNotFailIfUpstreamSegmentsPropertyDoesNotExists(){
        when(commander.getECProperty(sameString("/projects[WM Segment Registry]/SegmentRegistry/${projectName}:${procedureName}/upstreamSegments")))
            .thenReturn(Property.invalidProperty("/projects[WM Segment Registry]/SegmentRegistry/${projectName}:${procedureName}/upstreamSegments"))

        def ivyInfo = new IvyInfo(project)
        def registry = new SegmentRegistry(commander)

        registry.registerWithUpstreamSegments(ivyInfo)
    }

    @Test
    public void registerWithUpstreamSegments_shouldNotFailIfDownstreamSegmentsPropertyDoesNotExists(){
        addUpstreamDependency("org.gap.team:component", "Some Team's Project:Component Segment", project)
        arrangePriorUpstreamSegments("${projectName}:${procedureName}", "Some Team's Project:Component Segment")
        when(commander.getECProperty(sameString("/projects[WM Segment Registry]/SegmentRegistry/Some Team's Project:Component Segment/downstreamSegments")))
            .thenReturn(Property.invalidProperty("/projects[WM Segment Registry]/SegmentRegistry/Some Team's Project:Component Segment/downstreamSegments"))


        def ivyInfo = new IvyInfo(project)
        def registry = new SegmentRegistry(commander)

        registry.registerWithUpstreamSegments(ivyInfo)
        verify(commander).setECProperty(sameString("/projects[WM Segment Registry]/SegmentRegistry/Some Team's Project:Component Segment/downstreamSegments"), "${projectName}:${procedureName}".toString())

    }

    private void addUpstreamDependency(upstreamIdentifier, upStreamSegment, Project project) {
        project.configurations.create('configName')
        project.dependencies.add('configName', "${upstreamIdentifier}:+")
        when(commander.getECProperty(sameString("/projects[WM Segment Registry]/IdentifierRegistry/${upstreamIdentifier}/segment")))
            .thenReturn(new Property('/projects[WM Segment Registry]/IdentifierRegistry/org.gap.team:component/segment', upStreamSegment))
    }

    private void arrangePriorDownstreamSegments(segmentName, downstreamSegments) {
        when(commander.getECProperty(sameString("/projects[WM Segment Registry]/SegmentRegistry/${segmentName}/downstreamSegments")))
            .thenReturn(new Property("/projects[WM Segment Registry]/SegmentRegistry/Some Team's Project:Component Segment/downstreamSegments", downstreamSegments))
    }

    private void arrangePriorUpstreamSegments(segmentName, upstreamSegments) {
        when(commander.getECProperty(sameString("/projects[WM Segment Registry]/SegmentRegistry/${segmentName}/upstreamSegments")))
            .thenReturn(new Property("/projects[WM Segment Registry]/SegmentRegistry/Project Name:Procedure Name/ivyIdentifiers", upstreamSegments))
    }
}
