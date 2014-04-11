package com.gap.pipeline.tasks

import static org.mockito.Matchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

import com.gap.gradle.ivy.IvyInfo
import com.gap.pipeline.ec.SegmentRegistry
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class PopulateSegmentRegistryTaskTest {

    @Test
    public void execute_shouldPopulateRegistryAndRegisterWithUpstreamSegments(){
        def registry = mock(SegmentRegistry)
        def project = ProjectBuilder.builder().build()
        def project2 = ProjectBuilder.builder().withName("k").build()
        def task = new PopulateSegmentRegistryTask(project2, registry)
        def expectedIvyInfo = new IvyInfo(project)

        task.execute()

        verify(registry).populate(eq(expectedIvyInfo))
        verify(registry).registerWithUpstreamSegments(eq(expectedIvyInfo))

    }
}
