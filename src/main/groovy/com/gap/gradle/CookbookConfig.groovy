package com.gap.gradle

class CookbookConfig {
	def name
	def sha1Id
	CookbookConfig(def name, def sha1Id) {
		this.name = name
		this.sha1Id = sha1Id
	}
}
