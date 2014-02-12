package com.gap.gradle

/**
 * This bean represents the JSON in prodDeployParameters.json
 * 
 * @author krishnarangavajhala
 *
 */
class ProdDeployParameterConfig {
	def roleName
	CookbookConfig cookbook
	def deployECProcedure
	def nodes = []
	def sha1IdList = [] // array of sha Ids trimmed & sha1 regex matched.
	def isRPM // indicates if the deployable of this application is an rpm or the artifact itself.
	def appVersion // version of the application that should be deployed in prod node.
	
	ProdDeployParameterConfig() {
		// default constructor needed for retrieval
	}
	
	ProdDeployParameterConfig(ProdDeployConfig p) {
		this.roleName = p.roleName
		this.deployECProcedure = p.deployECProcedure
		this.nodes = p.nodes
		this.sha1IdList = p.sha1IdList
		this.isRPM = p.isRPM
		this.appVersion = p.appVersion
		this.cookbook = p.getCookbookConfig()
	}
}
