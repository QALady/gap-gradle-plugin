package com.gap.gradle.tasks

import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters

@RequiredParameters([
	@Require(parameter = 'athenaLocalRpmBase', description = "The base path of the local assembled RPM on the build box ready for upload.")
])
class UploadAthenaBuildRpmToRepo extends WatchmenTask {
	Project project
	def shellCommand
	def log = LogFactory.getLog(UploadAthenaBuildRpmToRepo)

	static def REPO_HOST = 'cibuild@repo1.phx.gapinc.dev'
	static def REPO_PATH = '/mnt/repos/stores/stores-custom-iss'	

	UploadAthenaBuildRpmToRepo(Project project, shellCommand = new ShellCommand()) {
		super(project)
		this.project = project
		this.shellCommand = shellCommand
	}

	def execute() {
		println "Athena rpmbase: " + project.athenaLocalRpmBase
		def numberOfRpms = this.shellCommand.execute(["ssh", REPO_HOST, "ls -1 $REPO_PATH/s2c_gap-pos*rpm | wc -l".toString()])
		if (numberOfRpms.asType(Integer) > 6) {
			println "There are $numberOfRpms s2c_gap-pos RPMs on repo1:"
			println this.shellCommand.execute(["ssh", REPO_HOST, "ls -1 $REPO_PATH/s2c_gap-pos*rpm".toString()])
			println "This project requires that there be 3 or fewer.  Please clean up the repo and try again."
		} else {
			def rpmName = new File("${project.athenaLocalRpmBase}/s2c_gap-pos-14.041.info").text		
			if (rpmName) {
				copyRpmToRepo(rpmName)
			}
		}
	}
	
	void copyRpmToRepo(rpmName) {
		println "Copying $rpmName to the stores-custom repos"
		// upload s2c_gap-pos-rpm:
		this.shellCommand.execute("scp ${project.athenaLocalRpmBase}/${rpmName} ${REPO_HOST}:${REPO_PATH}/${rpmName}")
		this.shellCommand.execute("scp ${project.athenaLocalRpmBase}/${rpmName} ${REPO_HOST}:/mnt/repos/stores/stores-custom-register/${rpmName}")

		// upload gap-pos-database rpm:
		def dbRpmName = new File("${project.athenaLocalRpmBase}/gap-pos-database-14.041.info").text
		println "Copying $dbRpmName to the stores-custom repo"
		this.shellCommand.execute("scp ${project.athenaLocalRpmBase}/${dbRpmName} ${REPO_HOST}:${REPO_PATH}/${dbRpmName}")

		println "Generating repo metadata"
		println this.shellCommand.execute(["ssh", REPO_HOST, "cd /mnt/repos/stores/stores-custom-iss; sudo /usr/bin/createrepo .".toString()])
		println this.shellCommand.execute(["ssh", REPO_HOST, "cd /mnt/repos/stores/stores-custom-register; sudo /usr/bin/createrepo .".toString()])
	}
}