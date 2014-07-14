package com.gap.pipeline

class ProdPrepareConfig {
	final static String FILE_NAME = "prodDeployParameters.json"
	final static def SHA1_PATTERN = /[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/
    def sha1Ids // comma separated list of sha Ids given in as input.
    def roleName
    def cookbookName
	def cookbookSha1Id
    def deployECProcedure
    def nodes = []
	def sha1IdList = [] // array of sha Ids trimmed & sha1 regex matched.
	def appVersion // version of the application that should be deployed in prod node.
    def yumSourceUrl
    def yumDestinationUrl
    def rpmNames = []
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
