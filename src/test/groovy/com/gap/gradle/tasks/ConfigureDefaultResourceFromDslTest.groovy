package com.gap.gradle.tasks

import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.utils.EnvironmentStub
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

class ConfigureDefaultResourceFromDslTest {
    private Project project
    private CommanderClient commanderClient
    private ShellCommand mockShellCommand
    def logger = LogFactory.getLog(ConfigureDefaultResourceFromDslTest)
    ConfigureDefaultResourceFromDsl task

    @Before
    void setup() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gap-wm-segmentdsl'

        mockShellCommand = mock(ShellCommand, Mockito.RETURNS_SMART_NULLS)

        EnvironmentStub env = new EnvironmentStub()
        env.setValue('COMMANDER_JOBID', '1234') // sets the job ID
        commanderClient = new CommanderClient(mockShellCommand, env)
        task = new ConfigureDefaultResourceFromDsl(project, commanderClient)

    }

    @Test
    void shouldSetResourceName(){
        def defaultResourcePath="/myJob/watchmen_config/config/defaultResource"

        project.segment {
            resourceName 'resource-name-01'
            prepare {
                smoke {
                    action 'WM Exec:Run'
                    parameters {
                        cmd.value './gradlew tasks --info'
                    }
                }
            }
        }
        task.execute()
        verify(mockShellCommand).execute(['ectool', 'setProperty', defaultResourcePath, 'resource-name-01'])
    }

    @Test
    void shouldSetDefaultResource(){
        def defaultResourcePath="/myJob/watchmen_config/config/defaultResource"

        project.segment {
            prepare {
                smoke {
                    action 'WM Exec:Run'
                    parameters {
                        cmd.value './gradlew tasks --info'
                    }
                }
            }
        }
        task.execute()
        verify(mockShellCommand).execute(['ectool', 'setProperty', defaultResourcePath, 'default'])
    }
}
