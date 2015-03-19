package com.gap.gradle.utils

import org.apache.commons.io.FilenameUtils
import org.gradle.api.Project

class FileDownloader {
    private final Project project

    public FileDownloader(Project project) {
        this.project = project
    }

    public File download(String url, File destinationDir) {
        if (!destinationDir.exists()) {
            destinationDir.mkdirs()
        }

        project.ant.get(src: url, dest: destinationDir.getAbsolutePath(), verbose: true)

        return new File(destinationDir, FilenameUtils.getName(url))
    }
}
