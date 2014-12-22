package com.gap.gradle.extensions

class GapWMSegmentDslJobLink {
	final String name
	def link

	public GapWMSegmentDslJobLink(String name) {
		this.name = name
	}

	@Override
	boolean equals(o) {
		if (this.is(o)) return true
		if (getClass() != o.class) return false

		GapWMSegmentDslJobLink that = (GapWMSegmentDslJobLink) o

		return name == that.name
	}
	
	@Override
	int hashCode() {
		return name.hashCode()
	}

	@Override
	public String toString() {
		return "GapWMSegmentDslJobLink{" +
				"name='" + name + '\'' +
				", link='" + link.toString() + '\'' +
				'}';
	}

}
