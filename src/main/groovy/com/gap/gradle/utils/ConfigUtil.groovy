package com.gap.gradle.utils

import org.gradle.api.Project

class ConfigUtil {

    def loadConfig(Project project, String filePath) {
        File configFile = new File(filePath)
        if (configFile.exists()) {
            Properties properties = new Properties()
            properties.load(new InputStreamReader(new FileInputStream(configFile)))
            properties.each {
                setConfigProperty(project, it.key, it.value)
            }
        }
    }

    /**
     * Sets value of config property by walking object graph to the leaf property.
     *
     * <p>Parameter {@code name} is a dot-separated name, such as {@code "jenkins.serverUrl"}. This
     * method walks from the root {@link Project project} object to the target leaf property
     * {@code serverUrl} and then assigns the {@code value} to it.</p>
     *
     * @param project The gradle project
     * @param name The dot-separated config name (i.e., jenkins.serverUrl)
     * @param value The value of the config property
     */
    def setConfigProperty(project, name, value) {
        def segments = name.split('\\.')
        def target = project
        // walk until the leaf property
        for (int i = 0; i < segments.size() - 1; i++) {
            target = target."${segments[i]}"
        }
        // set value on leaf property
        target."${segments.last()}" = value
    }

}