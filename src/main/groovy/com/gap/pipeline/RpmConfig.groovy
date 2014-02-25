package com.gap.pipeline

class RpmConfig {
	def yumSourceUrl
	def yumDestinationUrl
	def rpmNames
	def appVersion	

	RpmConfig() {
		
	}

	RpmConfig(def json) {
		this.appVersion = json.appVersion
		this.yumSourceUrl = json.yumSourceUrl
		this.yumDestinationUrl = json.yumDestinationUrl
		this.rpmNames = json.rpmNames
	}
}