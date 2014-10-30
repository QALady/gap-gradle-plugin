package com.gap.gradle.extensions

class GapWMSegmentDslAction {
	final String name
	String command
	
	public GapWMSegmentDslAction(String name) {
		this.name = name
	}
	
	@Override
	boolean equals(o) {
		if (this.is(o)) return true
		if (getClass() != o.class) return false

		GapWMSegmentDslAction that = (GapWMSegmentDslAction) o

		return name == that.name
	}
	
	@Override
	int hashCode() {
		return name.hashCode()
	}
	
	@Override
	public String toString() {
		return "GapWMSegmentDslAction{" +
				"name='" + name + '\'' +
				", command='" + command + '\'' +
				'}';
	}
}