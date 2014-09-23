package com.gap.pipeline.ec
import com.gap.gradle.utils.ShellCommandException
import com.gap.pipeline.utils.Environment
import com.gap.gradle.utils.ShellCommand
import org.slf4j.LoggerFactory

class CommanderClient {

  def shellCommand
  def environment
  def logger = LoggerFactory.getLogger(com.gap.pipeline.ec.CommanderClient)
  private final String PROJECT_NAME_PROPERTY = '/myJob/projectName'
  private final String PROCEDURE_NAME_PROPERTY = '/myJob/liveProcedure'

  CommanderClient(shellCommand = new ShellCommand(), environment = new Environment()) {
    this.shellCommand = shellCommand
    this.environment = environment
  }

  private def getCurrentProjectName(){
    getECProperty(PROJECT_NAME_PROPERTY).value
  }

  private def getCurrentProcedureName(){
    getECProperty(PROCEDURE_NAME_PROPERTY).value
  }

  def addLink(filename, jobid){
    def filenameWithoutExtension = filename.substring(0, filename.lastIndexOf('.'))
    setECProperty("/jobs[${jobid}]/report-urls/${filenameWithoutExtension}", "/commander/jobs/${jobid}/default/${filename}")
  }

  def addLinkToUrl(linkName, linkUrl, jobId = getJobId()){
    setECProperty("/jobs[${jobId}]/report-urls/${linkName}", linkUrl)
  }

  def addLinkToRunProcedureInJob(linkName, procedureName){
    addLinkToUrl(linkName, getRunProcedureUrl(procedureName), getJobId())
  }

  def getJobId() {
    environment.getValue('COMMANDER_JOBID')
  }

  def getCurrentJobDir (){
    environment.getValue('COMMANDER_WORKSPACE_UNIX')
  }

  def setDefaultParameterValue(fullProcedureName, parameterName, defaultValue) {
    def procedure = parseProcedureName(fullProcedureName)
    shellCommand.execute(['ectool', 'modifyFormalParameter', procedure.projectName, procedure.procedureName, parameterName, '--defaultValue', defaultValue])
  }

  def getRunProcedureUrl(fullProcedureName) {
    def procedure = parseProcedureName(fullProcedureName)
    URI uri = new URI(
      "https",
      "commander.gapinc.dev",
      "/commander/link/runProcedure/projects/${procedure.projectName}/procedures/${procedure.procedureName}", null);
    uri.toString()+"?s=Projects"
  }

  private parseProcedureName(String fullProcedureName) {
    def parts = fullProcedureName.split(':')
    if (parts.size() < 2) {
      throw new IllegalArgumentException("The procedure name '${fullProcedureName}' is invalid. It should be of the format '<project name>:<procedure name>'")
    }
    [projectName: parts[0], procedureName: parts[1]]
  }


  private setECProperty(name, value) {
    shellCommand.execute(['ectool', 'setProperty', name.toString(), value.toString()])
  }

  public def getECProperty(key) {
    try{
      return new Property(key, shellCommand.execute(['ectool', 'getProperty', key.toString()]))
    }
    catch (ShellCommandException e){
      if(e.message.contains('[NoSuchProperty]')){
        logger.debug("Requested property does not exist. ${e.message}\n")
        return Property.invalidProperty(key)
      }
      else throw e
    }
  }

  def getCurrentSegmentConfig(){
    new SegmentConfig(getSegmentConfigPropertyValue('configSCMUrl'),
    getSegmentConfigPropertyValue('workingDir'),
    getSegmentConfigPropertyValue('ciDir'),
    getSegmentConfigPropertyValue('gradleFile'),
    getSegmentConfigPropertyValue('scmConfigName'),
    isManualSegment())
  }

  private boolean isManualSegment() {
    def isManualProperty = getSegmentConfigProperty('config/isManual')
    def isManual = isManualProperty.isValid() ? Boolean.valueOf(isManualProperty.value) : false;
    isManual
  }

  private String getSegmentConfigPropertyValue(String property) {
    getSegmentConfigProperty(property).value
  }

  private Property getSegmentConfigProperty(String property) {
    getECProperty('/myJob/watchmen_config/' + property)
  }

  def getUserId(){
    getECProperty("/myJob/launchedByUser").value
  }

  def getUserName(){
    def jobTriggeredByUserId = getECProperty("/myJob/launchedByUser").value
    isJobTriggeredManually(jobTriggeredByUserId)? getECProperty("/users[$jobTriggeredByUserId]/fullUserName").value: jobTriggeredByUserId
  }

  def getStartTime(){
    getECProperty("/myJob/start").value
  }

  private static def isJobTriggeredManually(userId){
    !userId.toString().contains(' ')
  }

  def getCurrentSegment() {
    return new Segment(getCurrentProjectName(), getCurrentProcedureName())
  }

  def isRunningInPipeline(){
    getJobId() != null
  }

  def getArtifactoryUserName() {
    if(isRunningInPipeline()) {
      try {
        shellCommand.execute(["ectool", "getFullCredential", """/projects/WM Credentials/credentials/WMArtifactory""", "--value", """userName"""])
      } catch(ShellCommandException se) {
        logger.warn(se.getMessage())
        if(se.getMessage().contains('ectool error [InvalidCredentialName]')) {
          logger.warn("Using dummy credentials - Only WM Gralde:Invoke & WM Exec:Run are approved steps to access artifactory credentails.")
          return "dummy"
        } else if(se.getMessage().contains('ectool error [InvalidCredentialName]')) {
          logger.warn("Using dummy credentials - Credentials are not available outside of Watchmen Abstract Segment")
          return "dummy"
        } else {
          throw se
        }
      }
    } else {
        logger.warn("Using dummy credentials - Credentials are accesible only in pipline")
        return "dummy"
    }
  }

  def getArtifactoryPassword(){
    if(isRunningInPipeline()) {
      try {
        shellCommand.execute(["ectool", "getFullCredential", """/projects/WM Credentials/credentials/WMArtifactory""", "--value", """password"""])
      } catch(ShellCommandException se) {
        logger.warn(se.getMessage())
        if(se.getMessage().contains('ectool error [InvalidCredentialName]')) {
          logger.warn("Using dummy credentials - Only WM Gralde:Invoke & WM Exec:Run are approved steps to access artifactory credentails.")
          return "dummy"
        } else if(se.getMessage().contains('ectool error [InvalidCredentialName]')) {
          logger.warn("Using dummy credentials - Credentials are not available outside of Watchmen Abstract Segment")
          return "dummy"
        } else {
          throw se
        }
      }
    }
    else {
      logger.warn("Using dummy credentials - Credentials are accesible only in pipline")
      return "dummy"
    }
  }
}
