package com.gap.gradle.tasks

import org.junit.Test
import org.gradle.testfixtures.ProjectBuilder
import com.gap.pipeline.ProdDeployParameterConfig
import static org.hamcrest.CoreMatchers.is
import static org.junit.Assert.assertThat
import com.gap.pipeline.GitConfig
import com.gap.pipeline.CookbookConfig
import org.gradle.api.Project
import org.junit.Before

class PrepareToPromoteToProductionTaskTest {

    Project project

//    @Before
//    public void setUp() {
//        project = ProjectBuilder.builder().build()
//        project.extensions.create('prodDeploy', ProdDeployParameterConfig)
//        project.extensions.create('gitconfig', GitConfig)
//        project.setProperty('userId', 'id')
//        project.prodDeploy.cookbook = new CookbookConfig("name", "sha")
//
//        project.task('promoteCookbookBerksfile') << {}
//    }



}
