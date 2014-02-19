package com.gap.pipeline.tasks.annotations

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Retention(RetentionPolicy.RUNTIME)
public @interface Require {
    String parameter()
    String description()
}
