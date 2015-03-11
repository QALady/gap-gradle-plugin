package com.gap.gradle.plugins.airwatch

public interface BeginInstallConfig {
    String getAppName()
    String getAppDescription()
    String getPushMode()
    String getLocationGroupId()
    Integer getUploadChunks()
}
