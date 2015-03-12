package com.gap.gradle.plugins.mobile

import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.mockito.Matchers.anyVararg
import static org.mockito.Mockito.*

public class MobileDeviceUtilsTest {
    private CommandRunner commandRunner
    private MobileDeviceUtils mobileDevice

    @Before
    public void setUp() throws Exception {
        commandRunner = mock(CommandRunner)
        when(commandRunner.run(anyVararg())).thenReturn(" 123 ")

        mobileDevice = new MobileDeviceUtils(commandRunner)
    }

    @Test
    public void shouldListAttachedDevices() throws Exception {
        assertEquals("123", mobileDevice.listAttachedDevices())

        verify(commandRunner).run('bash', '-c', MobileDeviceUtils.LIST_ATTACHED_DEVICE)
    }
}
