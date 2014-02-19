package com.gap.gradle.plugins
import com.gap.gradle.plugins.cookbook.JenkinsConfig
import com.gap.gradle.tasks.DeployToProductionTask
import com.gap.gradle.tasks.PromoteRpmTask
import com.gap.gradle.tasks.PromoteToProductionTask
import com.gap.gradle.tasks.UpdateCookbookSHATask
import com.gap.gradle.utils.ConfigUtil
import com.gap.pipeline.GitConfig
import com.gap.pipeline.ProdDeployParameterConfig
import com.gap.pipeline.ProdPrepareConfig
import com.gap.pipeline.RpmConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
/**
 *
 * this plugin requires gapDeployTools:watchmen_config recipe to be run on a node
 * to prep up the node with pipeline.gradle & gapcookbook.properties 
 * the jenkins server urls of PROD Knife manage are expected to be configured in gapcookbook.properties  
 *
 */
class GapProdDeployPlugin implements Plugin<Project>{

    static def CONFIG_FILE = "${System.getProperty('user.home')}/.watchmen/gapcookbook.properties"

    @Override
	public void apply(Project project) {
	  
		project.apply plugin: 'gapcookbook'
		
		loadJenkinsConfig(project)
		loadProdDeployConfig(project)

		project.task('prepareToPromote') << {
			println "preparing to promote"
			//new PrepareToPromoteToProductionTask(project).execute() TODO reactivate when working
		}

		project.task('promoteToProduction', dependsOn: 'prepareToPromote') << {
			println "promoting to production"
			new PromoteToProductionTask(project).execute()
		}

		project.task('deployToProduction', dependsOn: 'promoteToProduction') << {
			println "deploying to production"
			new DeployToProductionTask(project).execute()
			println "WOO! done (:"
		}

        project.task('promoteRpm', dependsOn: 'prepareToPromote') << {
            new PromoteRpmTask(project).execute()
        }

		project.task('promoteCookbookBerksfile') << {
			new UpdateCookbookSHATask(project).execute()
		}

    }

	/**
	 * though this plugin does not have explicit dependency with gapcookbook
	 * the gapcookbook.properties file that has to be prepared on a node this deployment is being done
	 * is expected to be loaded for the prod infra jenkins server configuration(s).	
	 * 
	 * this method would ensure that project.jenkins extension container is loaded from gapcookbook.properties if not already loaded.
	 */
	private void loadJenkinsConfig(Project project) {
		def jenkinsExtension = project.extensions.findByName('jenkins')
		if (!jenkinsExtension) {
			project.extensions.create('jenkins', JenkinsConfig)
			new ConfigUtil().loadConfig(project, CONFIG_FILE)
		}
	}
	
	private void loadProdDeployConfig(Project project) {
		def json = new ConfigUtil().loadConfigFromJson(project.paramJsonPath + ProdPrepareConfig.FILE_NAME)
		def prodDeploy = project.extensions.findByName("prodDeploy")
		if (!prodDeploy && project.hasProperty('paramJsonPath')) {			
			project.extensions.create('prodDeploy', ProdDeployParameterConfig, json)
		} else {
			project.extensions.create('prodDeploy', ProdDeployParameterConfig)
		}

		// load git config
		loadGitConfig(project, json)

		project.extensions.create("rpm", RpmConfig, json.rpm)
	}

	private loadGitConfig(Project project, json) {
		def gitExtension = project.extensions.findByName("git")
		if (!gitExtension) {
			project.extensions.create('git', GitConfig, json.git)
		}
		project.git.userId = project.ecUser
	}
}
