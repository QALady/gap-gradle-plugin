package com.gap.gradle.extensions

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator

class GapWMSegmentDsl {
	private final Project project
	final GapWMSegmentDslAction myaction
 
	def company
	def title
	
	GapWMSegmentDsl(Project project, Instantiator instantiator) {
		this.project = project
		this.myaction = instantiator.newInstance(GapWMSegmentDslAction)
	}

	void myaction(Action<GapWMSegmentDslAction> action) {
		action.execute(myaction)
	}

}


