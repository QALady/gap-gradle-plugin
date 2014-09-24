package com.gap.gradle.tasks

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Ignore

class CopyUpStreamChangeLogDownStreamTaskTest {

	Project project
	Task task
	
	@Before
	void setUp() {
		project = ProjectBuilder.builder().build();
		project.apply plugin: 'gappipeline'
		task = project.tasks.findByName('copyUpstreamChangelogDownstream')
	}

	@Ignore
	void shouldRunExecute() {
		task.execute()
		"""<propertySheet>
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
    </propertySheet>"""
  
	}
}
