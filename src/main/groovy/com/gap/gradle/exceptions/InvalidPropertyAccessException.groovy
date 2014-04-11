package com.gap.gradle.exceptions

class InvalidPropertyAccessException extends RuntimeException{
    InvalidPropertyAccessException(GString gString) {
        super(gString)
    }
}
