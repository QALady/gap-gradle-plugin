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
		task = new GenerateGradleWMSegmentDslFromPropertiesTask(project, propertyFileName)
	}

	@Test
	void shouldReadPropertyFile() {
		task.readPropertiesFile()
		logger.info("Line count is ${task.lines.size()}")
		Assert.assertEquals("Line count does not match",task.lines.size(), 39)
	}

	@Test
	void shouldGradlelizeData() {
		def expectedData="""segment {
    'prepare' {
        'build_war' {
            'action' 'WM Exec:Run'
            'parameters' {
                'cmd' {
                    'value' './gradlew war'
                }
            }
        }
    }
    'test' {
        'unit_test' {
            'action' 'WM Exec:Run'
            'parameters' {
                'cmd' {
                    'value' './gradlew test'
                }
            }
        }
    }
    'approve' {
        '0-sonar' {
            'action' 'WM Exec:Run'
            'parameters' {
                'cmd' {
                    'value' './gradlew sonar -Pversion=\$[/myJob/version]'
                }
            }
        }
        '1-createLink' {
            'action' 'WM Publish:Apache Reports Copy'
            'runOrder' 'parallel'
            'runCondition' 'always'
            'parameters' {
                'Link Label' {
                    'value' 'domain-a Junit Results'
                }
                'Link Location' {
                    'value' '/reports/\$[jobId]_domain_a'
                }
                'reportSourceDirectory' {
                    'value' '\$[/myJob/watchmen_config/workingDir]/app-a/domain-a/build/reports/tests'
                }
            }
        }
        '2-createLink' {
            'action' 'WM Publish:Apache Reports Copy'
            'runOrder' 'parallel'
            'runCondition' 'always'
            'parameters' {
                'Link Label' {
                    'value' 'app-a Junit Results'
                }
                'Link Location' {
                    'value' '/reports/\$[jobId]_app_a'
                }
                'reportSourceDirectory' {
                    'value' '\$[/myJob/watchmen_config/workingDir]/app-a/webapp-a/build/reports/tests'
                }
            }
        }
        '3-createLink' {
            'action' 'WM Publish:Apache Reports Copy'
            'runOrder' 'parallel'
            'runCondition' 'always'
            'parameters' {
                'Link Label' {
                    'value' 'domain-b Junit Results'
                }
                'Link Location' {
                    'value' '/reports/\$[jobId]_domain_b'
                }
                'reportSourceDirectory' {
                    'value' '\$[/myJob/watchmen_config/workingDir]/app-b/domain-b/build/reports/tests'
                }
            }
        }
        '4-createLink' {
            'action' 'WM Publish:Apache Reports Copy'
            'runOrder' 'parallel'
            'runCondition' 'always'
            'parameters' {
                'Link Label' {
                    'value' 'app-b Junit Results'
                }
                'Link Location' {
                    'value' '/reports/\$[jobId]_app_b'
                }
                'reportSourceDirectory' {
                    'value' '\$[/myJob/watchmen_config/workingDir]/app-b/webapp-b/build/reports/tests'
                }
            }
        }
        '5-publish' {
            'action' 'WM Exec:Run'
            'parameters' {
                'cmd' {
                    'value' './gradlew packageRpm -Pversion=\$[/myJob/version]'
                }
            }
        }
        '6-publish' {
            'action' 'WM Publish:RPM'
            'parameters' {
                'channel' {
                    'value' 'devel'
                }
                'repoName' {
                    'value' 'watchmen'
                }
                'sourcePath' {
                    'value' 'build/distributions/ref-app-\$[/myJob/version]-1.noarch.rpm'
                }
            }
        }
        'upload_archives' {
            'action' 'WM Exec:Run'
            'parameters' {
                'cmd' {
                    'value' './gradlew uploadArchives -Pversion=\$[/myJob/version]'
                }
            }
        }
    }
    '_finally' {
        '0-final' {
            'action' 'test_final'
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

}
