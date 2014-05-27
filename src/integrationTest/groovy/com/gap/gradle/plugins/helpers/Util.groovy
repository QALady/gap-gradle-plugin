package com.gap.gradle.plugins.helpers

import com.gap.pipeline.utils.Environment

class Util {
    static def isRunningInPipeline() {
        new Environment().getValue('COMMANDER_HOME') != null
    }
}
