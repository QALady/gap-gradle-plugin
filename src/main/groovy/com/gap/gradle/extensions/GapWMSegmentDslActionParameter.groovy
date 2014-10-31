package com.gap.gradle.extensions

class GapWMSegmentDslActionParameter {
	String name
	String value
	
	GapWMSegmentDslActionParameter(String name, String value = '') {
		this.name = name
		this.value = value
	}
	
	public String toString() {
		return "{$name:$value}"
	}
}
