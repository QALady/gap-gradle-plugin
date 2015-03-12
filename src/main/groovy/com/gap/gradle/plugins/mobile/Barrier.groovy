package com.gap.gradle.plugins.mobile

import java.util.concurrent.TimeUnit

class Barrier {
    private final int numberOfTries
    private final long sleepTimeout
    private final TimeUnit sleepTimeUnit

    def Barrier(int maxNumberOfTries, long sleepTimeout, TimeUnit sleepTimeUnit) {
        this.numberOfTries = maxNumberOfTries
        this.sleepTimeout = sleepTimeout
        this.sleepTimeUnit = sleepTimeUnit
    }

    def executeUntil(Closure<Boolean> booleanClosure) {
        for (int counter = 0; counter < numberOfTries; counter++) {
            if (booleanClosure.call()) {
                break
            }

            if (counter+1 < numberOfTries) {
                sleepTimeUnit.sleep(sleepTimeout)
            } else {
                throw new MaxNumberOfTriesReached();
            }
        }
    }

    public class MaxNumberOfTriesReached extends RuntimeException {
    }
}
