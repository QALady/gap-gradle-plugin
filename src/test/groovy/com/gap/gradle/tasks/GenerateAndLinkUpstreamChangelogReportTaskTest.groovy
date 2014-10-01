package com.gap.gradle.tasks

import static org.junit.Assert.*
import groovy.xml.MarkupBuilder

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GenerateAndLinkUpstreamChangelogReportTaskTest {

	Project project
	GenerateAndLinkUpstreamChangelogReportTask task

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder()
	
	@Before
	void setUp() {
		project = ProjectBuilder.builder().withProjectDir(new File(temporaryFolder.root.path)).build();
		project.apply plugin: 'gappipeline'
		//task = project.tasks.findByName('linkUpstreamChangelogReport')
		task = new GenerateAndLinkUpstreamChangelogReportTask(project)
	}

	@Ignore
	void shouldRunExecute() {
		task.execute()
	}

	@Ignore
	void shouldCreateChangeListFile() {
		def data = new XmlSlurper().parseText(getECPropertySheetResp())
		def properties = data.property
		assert 2 == properties.size()
		task.createChangelistFile(properties)
		println new File("$temporaryFolder.root.path/UpStream_ChangeList_Report.html").toString()
	}
	
	@Ignore
	void shouldGenerateHtml() {
		def props = [[propertyName: "prop1", value: "value1"], [propertyName: "prop2", value: "value2"]]
		props.each { p ->
			println p.propertyName.toString()
			println p.value.toString()
		}

		StringWriter out = new StringWriter()
		def builder = new MarkupBuilder(out)
		builder.html {
			head {
				title "test html"
			}
			body {
				h1"Upstream ChangeLog Report"
				h2 {
					table {
						tr {
							td {
								a(href: '/commander/link/jobDetails/jobs/b406d50c-48e3-11e4-aa20-00505625f614', "Upstream Ecscm ChangeLog Trigger Segment Job Link")
							}
						}
						tr {
							td {
								a(href: '/commander/link/jobDetails/jobs/b406d50c-48e3-11e4-aa20-00505625f614', "This Report Segment Job Link")
							}
						}
					}
				}
				props.each { prop ->
				p {
					table {
						tr {
							td {
								mkp.yield prop.propertyName.toString()
							}
							td {
								b prop.value.toString()
							}
						}
					}
				  }
				}
				p "some more text"
			}
		}
		println out.toString()
	}
	
	private String getECPropertySheetResp() {
		return 		"""<propertySheet>
      <propertySheetId>c7cade2e-429c-11e4-9966-00505625f614</propertySheetId>
      <createTime>2014-09-22T21:09:46.212Z</createTime>
      <lastModifiedBy>Kr8s8k9</lastModifiedBy>
      <modifyTime>2014-09-22T21:11:26.826Z</modifyTime>
      <owner>Kr8s8k9</owner>
      <property>
        <propertyId>fe3b5496-429c-11e4-ba82-00505625f614</propertyId>
        <propertyName>prop1</propertyName>
        <createTime>2014-09-22T21:11:17.542Z</createTime>
        <description />
        <expandable>0</expandable>
        <lastModifiedBy>Kr8s8k9</lastModifiedBy>
        <modifyTime>2014-09-22T21:11:17.542Z</modifyTime>
        <owner>Kr8s8k9</owner>
        <value>val1</value>
      </property>
      <property>
        <propertyId>03c4b81f-429d-11e4-b9b8-00505625f614</propertyId>
        <propertyName>prop2</propertyName>
        <createTime>2014-09-22T21:11:26.826Z</createTime>
        <description />
        <expandable>1</expandable>
        <lastModifiedBy>Kr8s8k9</lastModifiedBy>
        <modifyTime>2014-09-22T21:11:26.826Z</modifyTime>
        <owner>Kr8s8k9</owner>
        <value>val2</value>
      </property>
    </propertySheet>
"""

	}
}
