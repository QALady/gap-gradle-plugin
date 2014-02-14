package com.gap.gradle.plugins.resources

class GapResourcesExtension {
  String filePattern
  String dataExt
  String fileExt

  GapResourcesExtension(String filePattern, String fileExt, String dataExt) {
    this.filePattern = filePattern
    this.dataExt = dataExt
    this.fileExt = fileExt
  }
}
