package com.gap.pipeline

class GitConfig {
	def sha1Id
	def fullRepoName

	GitConfig () {
		
	}

	GitConfig(def json) {
		this.sha1Id = json.sha1Id
		this.fullRepoName = json.fullRepoName
	}
	
	GitConfig(def sha1Id, def fullRepoName) {
		this.sha1Id = sha1Id
		this.fullRepoName = fullRepoName
	}
	
}
