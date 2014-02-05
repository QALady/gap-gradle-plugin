package com.gap.gradle.plugins.cookbook

class ParameterConfig {
	// this is just a place holder closure for loading the 'parameters'
	// extensions from the build script while applying the GapCookbookPlugin.
	// the members of this ParameterConfig are dynamically binded into this class
	// by groovy while loading the parameters from the config properties.
	
	// this property lists a comma separated property names that this build have to pass to Jenkins Job in the config file.
	def parameterNames
}
