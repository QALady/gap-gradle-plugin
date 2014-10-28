package com.gap.gradle.extensions

class WatchmenDSLExtension {
	Segment segment

	class Segment {
		boolean allowConcurrentSegments
		List<Actions> actions

	}

	class Steps {
		String name
		String command
		Plugin plugin
	}

	class Actions {
		String resource
		List<Steps> steps
	}

	class Plugin{
		String action
		RunOrderEnum runOrder
		RunConditionEnum runCondition
		List<Parameters> parameters
	}

	class Parameters
	{
		String[] parameter
	}
	/**
	 * Created by frgonzalez on 10/27/14.
	 */
	public static enum RunOrderEnum {
		PARALLEL, NON_PARALLEL
	}
	/**
	 * Created by frgonzalez on 10/27/14.
	 */
	public static enum RunConditionEnum {
		ALWAYS, NEVER
	}
}


