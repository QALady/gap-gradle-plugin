package com.gap.pipeline.tasks

import com.gap.pipeline.ec.CommanderClient
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

/**
 *Created by Cl3s1l0 on 12-07-14.
 *
 * This task will replace the actual InsertResolvedVersion EC Plugin
 *
 * Insert the resolved static versions into the gradle-generated ivy.xml file.
 *
 */
class InsertResolvedVersionTask extends WatchmenTask {

    def log = LogFactory.getLog(InsertResolvedVersionTask)

    def Project project
    String ivyXmlPath = "${project.rootDir.path}/build/ivy.xml"

    def configurations = []
    def artifactInfo = [:]

    CommanderClient commanderClient

    InsertResolvedVersionTask(Project project, commanderClient = new CommanderClient()) {
        super(project)
        this.project = project
        this.commanderClient = commanderClient
    }

    def execute() {

        //check if ivy.xml file exist in t ci/build folder
        def ivyXml = new File(ivyXmlPath)
        if(ivyXml.exists()){

            configurations = get_configurations()
            modify_ivy_xml()

        } else {

            log.info "----------- ivy.xml doesn't exist, skipping task... ------------"
        }
    }

    //this method should be overrided in the unit test
    @Override
    def get_configurations(){

        def configurations = []

        project.configurations.each{ conf ->

            def configuration = [:]
            configuration['name'] = conf.name
            configuration['dependencies'] = []

            project.configurations[conf.name].allDependencies.each{ dep ->

                def dependency = [:]
                dependency['org'] = dep.group
                dependency['rev'] = dep.version
                dependency['name'] = dep.name

                configuration['dependencies'].push(dependency)
            }
            configurations.push(configuration)
        }

        return configurations
    }

    private void modify_ivy_xml() {

        //reading the text from ivy.xml
        String ivyXmlTex = new File(ivyXmlPath).text

        //modifying the text
        configurations.each { conf ->
            conf['dependencies'].each { dep ->

                def newLine = "<dependency org=\"${dep['org']}\" name=\"${dep['name']}\" rev=\"${dep['rev']}\" conf=\"${conf['name']}"
                def actualLine = /<dependency org="${dep['org']}" name="${dep['name']}" rev="\+" conf="${conf['name']}/

                ivyXmlTex = ivyXmlTex.replaceFirst(actualLine.toString(), newLine)
            }
        }

        //writing the new text into ivy.xml
        new File(ivyXmlPath).withWriter { out ->
            ivyXmlTex.eachLine { line ->
                out.writeLine(line)
            }
        }

        //finally it gets the artifact info from the ivy.xml file
        ivyXmlTex.eachLine {
            if(it =~ /<info /) {
                artifactInfo['organisation'] = (it =~ /organisation="[^"]*/)[0].toString().replace("organisation=\"","").replaceAll(/\./,"/")
                artifactInfo['module'] = (it =~ /module="[^"]*/)[0].toString().replace("module=\"","")
                artifactInfo['revision'] = (it =~ /revision="[^"]*/)[0].toString().replace("revision=\"","")
            }
        }
        artifactInfo['path'] = "${artifactInfo['organisation']}/${artifactInfo['module']}/${artifactInfo['revision']}/ivy-${artifactInfo['revision']}.xml"

        log.info "\n\n${ivyXmlTex}\n"
    }
}