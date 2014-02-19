package com.gap.pipeline

class CookbookConfig {
	def name
	def sha1Id
	CookbookConfig(def name, def sha1Id) {
		this.name = name
		this.sha1Id = sha1Id
	}
}
