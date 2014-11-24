package com.gap.gradle.extensions

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator


class GapWMSegmentDsl {
	public static final def segmentPhases = ['prepare', 'test', 'approve', '_finally']
	final NamedDomainObjectSet<GapWMSegmentDslAction> prepare
	final NamedDomainObjectSet<GapWMSegmentDslAction> test
	final NamedDomainObjectSet<GapWMSegmentDslAction> approve
	final NamedDomainObjectSet<GapWMSegmentDslAction> _finally
	def resourceName // resource definition at segment level.

	GapWMSegmentDsl(Project project, Instantiator instantiator) {
		this.prepare = project.container(GapWMSegmentDslAction, { name -> instantiator.newInstance(GapWMSegmentDslAction, name, project, instantiator) })
		this.test = project.container(GapWMSegmentDslAction, { name -> instantiator.newInstance(GapWMSegmentDslAction, name, project, instantiator) })
		this.approve = project.container(GapWMSegmentDslAction, { name -> instantiator.newInstance(GapWMSegmentDslAction, name, project, instantiator) })
		this._finally = project.container(GapWMSegmentDslAction, { name -> instantiator.newInstance(GapWMSegmentDslAction, name, project, instantiator) })
	}

	void prepare(Action<? super NamedDomainObjectCollection<GapWMSegmentDslAction>> action) {
		action.execute(prepare)
	}

	void test(Action<? super NamedDomainObjectCollection<GapWMSegmentDslAction>> action) {
		action.execute(test)
	}

	void approve(Action<? super NamedDomainObjectCollection<GapWMSegmentDslAction>> action) {
		action.execute(approve)
	}

	void _finally(Action<? super NamedDomainObjectCollection<GapWMSegmentDslAction>> action) {
		action.execute(_finally)
	}

	def getResourceName() {
		return resourceName
	}

}


