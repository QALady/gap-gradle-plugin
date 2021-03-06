package com.gap.pipeline

class ProdDeployParameterConfig {
	def roleName
	CookbookConfig cookbook
	def nodes = []
	def sha1IdList = [] // array of sha Ids trimmed & sha1 regex matched.
	def appVersion // version of the application that should be deployed in prod node.
	RpmConfig rpm
	GitConfig git

	ProdDeployParameterConfig() {
		// intentionally empty
	}

	ProdDeployParameterConfig(ProdPrepareConfig p) {
		this.roleName = p.roleName
		this.nodes = toList(p.nodes)
		this.sha1IdList = p.sha1IdList
		this.appVersion = p.appVersion
		this.cookbook = p.getCookbookConfig()
		this.rpm = p.getRpmConfig()
        this.git = p.getGitConfig()
	}

	/**
	 * Creates a new instance from the prodDeployParameters.json artifact.
	 * constructor used during deserialization of the json into object.
	 * @param json
	 */
	ProdDeployParameterConfig(def json) {
		this.roleName = json.roleName
		this.nodes = json.nodes
		this.sha1IdList = json.sha1IdList
		this.appVersion = json.appVersion
		this.cookbook = new CookbookConfig(json.cookbook.name, json.cookbook.sha1Id)
		this.git = new GitConfig(json)
		this.rpm = new RpmConfig(json)
	}

    static def toList(def input) {
        if (input) {
            def output = []
            input.eachLine { line ->
                line.split(',').each { entry ->
                    if (entry.trim()) {
                        output << entry.trim()
                    }
                }
            }
            output
        } else {
            []
        }
    }
}
