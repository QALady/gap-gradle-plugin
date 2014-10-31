package com.gap.gradle.extensions

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator

class GapWMSegmentDslAction {
	final String name
	def action
	final NamedDomainObjectSet<GapWMSegmentDslActionParameter> parameters
	
	public GapWMSegmentDslAction(String name, Project project, Instantiator instantiator) {
		this.name = name
		this.parameters = project.container(GapWMSegmentDslActionParameter, {pName -> instantiator.newInstance(GapWMSegmentDslActionParameter, pName)})
	}

	void parameters(Action<? super NamedDomainObjectCollection<GapWMSegmentDslActionParameter>> action) {
		action.execute(parameters)
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
				", action='" + action.toString() + '\'' +
				", parameters='" + parameters.toString() + '\'' +
				'}';
	}
}