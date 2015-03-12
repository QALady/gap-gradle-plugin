package com.gap.gradle.plugins.mobile

import com.gap.gradle.plugins.airwatch.util.CommandRunner
import org.junit.Before
import org.junit.Test

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

public class MobileDeviceUtilsTest {
    public static final String IDEVICE_ID_TOOL = '/usr/local/bin/idevice_id'

    private CommandRunner commandRunner
    private MobileDeviceUtils mobileDevice

    @Before
    public void setUp() throws Exception {
        commandRunner = mock(CommandRunner)
        mobileDevice = new MobileDeviceUtils(commandRunner)
    }

    @Test
    public void shouldListAttachedDevices() throws Exception {
        mobileDevice.listAttachedDevices()

        verify(commandRunner).run(IDEVICE_ID_TOOL, '--list')
    }
}
