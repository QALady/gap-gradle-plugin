package com.gap.gradle.exceptions

class RetryTimeOutException extends Exception {
	RetryTimeOutException() {
		super()
	}

	RetryTimeOutException(String message) {
		super(message)
	}
}
