package com.gap.gradle.airwatch

public interface BeginInstallConfig {
    String getAppName()
    String getAppDescription()
    String getPushMode()
    String getLocationGroupId()
    Integer getTotalChunks()
}