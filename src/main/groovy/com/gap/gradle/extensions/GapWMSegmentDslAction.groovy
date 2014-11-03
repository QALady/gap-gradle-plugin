package com.gap.gradle.extensions

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator

class GapWMSegmentDslAction {
	final String name
	def action
	def runCondition
	def runOrder
	final NamedDomainObjectSet<GapWMSegmentDslActionParameter> parameters
	
	public GapWMSegmentDslAction(String name, Project project, Instantiator instantiator) {
		this.name = name
		this.parameters = project.container(GapWMSegmentDslActionParameter, {pName -> instantiator.newInstance(GapWMSegmentDslActionParameter, pName)})
	}

	void parameters(Action<? super NamedDomainObjectCollection<GapWMSegmentDslActionParameter>> action) {
		action.execute(parameters)
	}

	boolean hasSubProject() {
		return this.action.toString().find(~/\:/)
	}

	public String getECStepRunCondition() {
		return (this.runCondition == 'always' || this.runCondition == 'finally') ? '' : '$[/myProject/runCondition]'
	}

	public String getECParallelStep() {
		return (this.runOrder == 'parallel') ? 'true' : 'false'
	}

	public String getECParameters() {
		StringBuffer ecParameters = new StringBuffer()
		this.parameters.each { param ->
			ecParameters.append('"' + param.name.toString().trim() + '"')
			ecParameters.append("=")
			ecParameters.append('"' + param.value.toString().trim() + '"')
			ecParameters.append(" ")
		}
		return ecParameters.toString()
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