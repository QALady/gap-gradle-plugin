package com.gap.gradle.plugins.xcode.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class TransformJUnitXmlReportToHTMLTask extends DefaultTask {

    String sourceFilePath
    String targetFilePath

    private String targetDir
    private Node testResults

    @TaskAction
    def convertReport() {
        targetDir = project.file(sourceFilePath).getParent()
        testResults = new XmlParser().parse(sourceFilePath)

        project.configurations {
            junitXmlToHtml
        }

        project.dependencies {
            junitXmlToHtml 'org.apache.ant:ant-junit:1.8.2'
        }

        assertUnitTestsWereExecuted()
        splitTestSuites()
        convertJUnitXmlToHtml()
    }

    private assertUnitTestsWereExecuted() {
        def anyTestSuiteWithoutTests = testResults.testsuite.@tests.any { Integer.valueOf(it) <= 0 }

        if (anyTestSuiteWithoutTests) {
            throw new GradleException("Test results shows that no tests were executed! Please have a look in the logs to check why that happened.")
        }
    }

    private splitTestSuites() {
        def dest = targetDir

        testResults.testsuite.eachWithIndex { suite, idx ->
            def fileWriter = new FileWriter(project.file("${dest}/TEST-${idx}.xml"))
            def printWriter = new PrintWriter(fileWriter)

            new XmlNodePrinter(printWriter).print(suite)
        }
    }

    private convertJUnitXmlToHtml() {
        def dest = targetDir

        project.ant.taskdef(
                name: 'junitreport',
                classname: 'org.apache.tools.ant.taskdefs.optional.junit.XMLResultAggregator',
                classpath: project.configurations.junitXmlToHtml.asPath
        )

        project.ant.junitreport(todir: dest) {
            fileset(dir: dest, includes: 'TEST-*.xml')
            report(todir: dest, format: 'noframes')
        }

        project.file("${targetDir}/junit-noframes.html").renameTo(targetFilePath)
    }

}
