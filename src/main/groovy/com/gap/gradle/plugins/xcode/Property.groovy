package com.gap.gradle.plugins.xcode

final class Property<T> {

    private final T value

    Property(T value) {
        this.value = value
    }

    T get() {
        if (value instanceof Closure) {
            return value.call()
        }

        value
    }
}
