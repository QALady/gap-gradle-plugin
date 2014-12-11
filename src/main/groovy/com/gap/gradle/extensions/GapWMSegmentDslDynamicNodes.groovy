package com.gap.gradle.extensions

class GapWMSegmentDslDynamicNodes {
	def name
	def openstackTenant
	def chefRole

	public GapWMSegmentDslDynamicNodes(String name) {
		this.name = name
	}

	boolean equals(o) {
		if (this.is(o)) return true
		if (getClass() != o.class) return false

		GapWMSegmentDslDynamicNodes that = (GapWMSegmentDslDynamicNodes) o

		if (name != that.name) return false

		return true
	}

	int hashCode() {
		return name.hashCode()
	}


	@Override
	public String toString() {
		return "GapWMSegmentDslDynamicNodes{" +
				"name=" + name +
				", openstackTenant=" + openstackTenant +
				", chefRole=" + chefRole +
				'}';
	}
}