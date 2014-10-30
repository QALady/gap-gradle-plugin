package com.gap.gradle.extensions

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator

import com.gap.gradle.airwatch.Environment;


class GapWMSegmentDsl {
	private final Project project
	final NamedDomainObjectSet<GapWMSegmentDslAction> actions
	//GapWMSegmentDslAction myaction

	GapWMSegmentDsl(Project project, Instantiator instantiator) {
		this.project = project
		this.actions = project.container(GapWMSegmentDslAction, { name -> instantiator.newInstance(GapWMSegmentDslAction, name) })
		//this.myaction instantiator.newInstance(GapWMSegmentDslAction)
	}

	/*
	void myaction(Action<GapWMSegmentDslAction> action) {
		action.execute(myaction)
	}*/

	void actions(Action<? super NamedDomainObjectCollection<GapWMSegmentDslAction>> action) {
		action.execute(actions)
	}
}


