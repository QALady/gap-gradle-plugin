package com.gap.pipeline

import com.gap.pipeline.CookbookConfig


class ProdPrepareConfig {
	final static String FILE_NAME = "prodDeployParameters.json"
	final static def SHA1_PATTERN = /\b([a-f0-9]{40})\b/
    def sha1Ids // comma separated list of sha Ids given in as input.
    def roleName
    def cookbookName
	def cookbookSha1Id
    def deployECProcedure
    def nodes = []
	def sha1IdList = [] // array of sha Ids trimmed & sha1 regex matched.
	def isRPM // indicates if the deployable of this application is an rpm or the artifact itself.
	def appVersion // version of the application that should be deployed in prod node.
    def yumSourceUrl
    def yumDestinationUrl
    def rpmName
	def githubOrgName

	CookbookConfig getCookbookConfig() {
		new CookbookConfig(this.cookbookName, this.cookbookSha1Id)
	}

	RpmConfig getRpmConfig() {
		new RpmConfig(this)
	}

	GitConfig getGitConfig() {
		def fullRepoName = "${this.githubOrgName}/${this.cookbookName}"
		new GitConfig(this.cookbookSha1Id, fullRepoName)
	}
}
