package com.gap.pipeline.tasks
import com.gap.pipeline.exception.MissingParameterException
import com.gap.pipeline.tasks.annotations.RequiredParameters

abstract class WatchmenTask {
    def project

    WatchmenTask(project){
        this.project = project
    }
    def validate(){
        def annotations = getClass().getAnnotation(RequiredParameters)
        if (annotations){
            annotations.value().each { annotation ->
                if (!this.hasProperty(project, annotation.parameter()) && !hasExtensionProperty(annotation.parameter())){
                    throw new MissingParameterException("Missing required parameter: '${annotation.parameter()}'")
                }
            }
        }
    }

    static def hasProperty(project, prop) {
        project.hasProperty(prop) && project[prop]
    }

    Boolean hasExtensionProperty(property) {
        def parts = property.split("\\.")
        def projectProperty = project
        for(part in parts ){
            if (!this.hasProperty(projectProperty, part)){
                return false
            }
            projectProperty = projectProperty[part]
        }
        return true
    }
}
