package com.gap.gradle.plugins

import com.gap.gradle.extensions.GapCloudResource;
import com.gap.gradle.tasks.CreateEasyCloudResourceTask
import com.gap.gradle.tasks.DeleteEasyCloudResourceTask
import com.gap.pipeline.ec.CommanderClient;

import org.gradle.api.Plugin
import org.gradle.api.Project

class GapCloudResourcePlugin implements Plugin<Project> {
    
    private static final String GAP_CLOUD_JSON_URL = "/server/watchmen_config/gapCloudJsonUrl";
    private CommanderClient commander = new CommanderClient();

    @Override
    public void apply(Project project)  {
        
        def url = commander.getECProperty(GAP_CLOUD_JSON_URL).value
        
        project.extensions.create("gapCloud", GapCloudResource)
        project.gapCloud.jsonUrl = url
        
        project.task('easyCreateCloudResource') << {
            new CreateEasyCloudResourceTask(project).createEasyCloudResource()         
        }  
        
        project.task('easyDeleteCloudResource') << {
            new DeleteEasyCloudResourceTask(project).deleteEasyCloudResource()
        }
        
        project.task('easyDeleteECResource') << {
            new DeleteEasyCloudResourceTask(project).deleteEasyECResource()
        }
    }
}
