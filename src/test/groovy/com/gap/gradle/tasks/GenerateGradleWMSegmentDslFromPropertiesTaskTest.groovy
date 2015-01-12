package com.gap.gradle.tasks

import org.apache.commons.logging.LogFactory
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.testng.Assert.assertEquals

class GenerateGradleWMSegmentDslFromPropertiesTaskTest {
	private Project project
	GenerateGradleWMSegmentDslFromPropertiesTask task
	def logger = LogFactory.getLog(GenerateGradleWMSegmentDslFromPropertiesTaskTest)
	final String propertyFileName = "src/test/groovy/com/gap/gradle/resources/component-segment.properties"
	final String expectedGradleFileName = "src/test/groovy/com/gap/gradle/resources/component-segment.gradle"

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder()

	@Before
	void setup() {
		project = ProjectBuilder.builder().withProjectDir(new File(temporaryFolder.root.path)).build();
		project.apply plugin: 'gap-wm-segmentdsl'
		project.segmentPropertiesFile = propertyFileName
		task = new GenerateGradleWMSegmentDslFromPropertiesTask(project)
	}

	@Test
	void shouldReadPropertyFile() {
		task.readPropertiesFile()
		logger.info("Line count is ${task.lines.size()}")
		Assert.assertEquals("Line count does not match", 39, task.lines.size())
	}

	@Test
	void shouldGradlelizeData() {
		def expectedData = """segment {
    prepare {
        build_war {
            action 'WM Exec:Run'
            parameters {
                cmd {
                    value './gradlew war'
                }
            }
        }
    }
    test {
        unit_test {
            action 'WM Exec:Run'
            parameters {
                cmd {
                    value './gradlew test'
                }
            }
        }
    }
    approve {
        '0_sonar' {
            action 'WM Exec:Run'
            parameters {
                cmd {
                    value './gradlew sonar -Pversion=\$[/myJob/version]'
                }
            }
        }
        '1_createLink' {
            action 'EC-FileOps:Remote Copy - Native'
            workspaceName 'chefLocalAgents'
            runOrder 'parallel'
            runCondition 'always'
            parameters {
                'Link Label' {
                    value 'domain-a Junit Results'
                }
                'Source Resource' {
                    value '\$[/myJobStep/resourceName]'
                }
                'Source Workspace' {
                    value 'chefLocalAgents'
                }
                'Source Path' {
                    value '\$[/myJob/watchmen_config/workingDir]/app-a/domain-a/build/reports/tests'
                }
                'Destination Resource' {
                    value 'local'
                }
                'Destination Workspace' {
                    value 'nfs'
                }
                'Destination Path' {
                    value '\$[/server/watchmen_config/sharedHtdocs]/reports/\$[jobId]_domain_a'
                }
            }
        }
        '2_createLink' {
            action 'EC-FileOps:Remote Copy - Native'
            workspaceName 'chefLocalAgents'
            runOrder 'parallel'
            runCondition 'always'
            parameters {
                'Link Label' {
                    value 'app-a Junit Results'
                }
                'Source Resource' {
                    value '\$[/myJobStep/resourceName]'
                }
                'Source Workspace' {
                    value 'chefLocalAgents'
                }
                'Source Path' {
                    value '\$[/myJob/watchmen_config/workingDir]/app-a/webapp-a/build/reports/tests'
                }
                'Destination Resource' {
                    value 'local'
                }
                'Destination Workspace' {
                    value 'nfs'
                }
                'Destination Path' {
                    value '\$[/server/watchmen_config/sharedHtdocs]/reports/\$[jobId]_app_a'
                }
            }
        }
        '3_createLink' {
            action 'EC-FileOps:Remote Copy - Native'
            workspaceName 'chefLocalAgents'
            runOrder 'parallel'
            runCondition 'always'
            parameters {
                'Link Label' {
                    value 'domain-b Junit Results'
                }
                'Source Resource' {
                    value '\$[/myJobStep/resourceName]'
                }
                'Source Workspace' {
                    value 'chefLocalAgents'
                }
                'Source Path' {
                    value '\$[/myJob/watchmen_config/workingDir]/app-b/domain-b/build/reports/tests'
                }
                'Destination Resource' {
                    value 'local'
                }
                'Destination Workspace' {
                    value 'nfs'
                }
                'Destination Path' {
                    value '\$[/server/watchmen_config/sharedHtdocs]/reports/\$[jobId]_domain_b'
                }
            }
        }
        '4_createLink' {
            action 'EC-FileOps:Remote Copy - Native'
            workspaceName 'chefLocalAgents'
            runOrder 'parallel'
            runCondition 'always'
            parameters {
                'Link Label' {
                    value 'app-b Junit Results'
                }
                'Source Resource' {
                    value '\$[/myJobStep/resourceName]'
                }
                'Source Workspace' {
                    value 'chefLocalAgents'
                }
                'Source Path' {
                    value '\$[/myJob/watchmen_config/workingDir]/app-b/webapp-b/build/reports/tests'
                }
                'Destination Resource' {
                    value 'local'
                }
                'Destination Workspace' {
                    value 'nfs'
                }
                'Destination Path' {
                    value '\$[/server/watchmen_config/sharedHtdocs]/reports/\$[jobId]_app_b'
                }
            }
        }
        '5_publish' {
            action 'WM Exec:Run'
            parameters {
                cmd {
                    value './gradlew packageRpm -Pversion=\$[/myJob/version]'
                }
            }
        }
        '6_publish' {
            action 'WM Publish:RPM'
            parameters {
                channel {
                    value 'devel'
                }
                repoName {
                    value 'watchmen'
                }
                sourcePath {
                    value 'build/distributions/ref-app-\$[/myJob/version]-1.noarch.rpm'
                }
            }
        }
        upload_archives {
            action 'WM Exec:Run'
            parameters {
                cmd {
                    value './gradlew uploadArchives -Pversion=\$[/myJob/version]'
                }
            }
        }
    }
    jobLinks {
        'domain-a Junit Results' {
            link '/reports/\$[jobId]_domain_a'
        }
        'app-a Junit Results' {
            link '/reports/\$[jobId]_app_a'
        }
        'domain-b Junit Results' {
            link '/reports/\$[jobId]_domain_b'
        }
        'app-b Junit Results' {
            link '/reports/\$[jobId]_app_b'
        }
    }
    _finally {
        '0_final' {
            action 'test_final'
        }
    }
}"""
		task.readPropertiesFile()
		task.parseToGradle()
		logger.info "\ngradlelizedData -> " + task.gradlelizedData
		assertEquals(task.gradlelizedData, expectedData)
	}

	@Test
	void shouldWriteToFile() {
		task.readPropertiesFile()
		task.parseToGradle()
		task.writeToGradleFile()
		File expectedGradleFile = new File(expectedGradleFileName)
		assertEquals(task.gradlelizedFile.getText(), expectedGradleFile.getText())
	}

	@Test
	void shouldQuoteIfSpaceOrNumber() {
		String case1 = '0-case'
		String expected1 = "'0-case'"
		String actual1 = task.quoteIfSpacesOrNumber(case1)
		logger.info("$actual1==$expected1")
		assertEquals(actual1, expected1)

		String case2 = 'case'
		String expected2 = "case"
		String actual2 = task.quoteIfSpacesOrNumber(case2)
		logger.info("$actual2==$expected2")
		assertEquals(actual2, expected2)

		String case3 = '02case'
		String expected3 = "'02case'"
		String actual3 = task.quoteIfSpacesOrNumber(case3)
		logger.info("$actual3==$expected3")
		assertEquals(actual3, expected3)

		String case4 = 'case1'
		String expected4 = "case1"
		String actual4 = task.quoteIfSpacesOrNumber(case4)
		logger.info("$actual4==$expected4")
		assertEquals(actual4, expected4)

		String case5 = '2 case'
		String expected5 = "'2 case'"
		String actual5 = task.quoteIfSpacesOrNumber(case5)
		logger.info("$actual5==$expected5")
		assertEquals(actual5, expected5)

		String case6 = 'case 1'
		String expected6 = "'case 1'"
		String actual6 = task.quoteIfSpacesOrNumber(case6)
		logger.info("$actual6==$expected6")
		assertEquals(actual6, expected6)
	}

}
