package com.gap.gradle.tasks

import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.ec.SegmentRegistry
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

import java.text.SimpleDateFormat

@RequiredParameters([
		@Require(parameter = 'segmentIdentifier', description = 'segmentIdentifier that describes the Project:Procedure to kick off manual segment of. <project name>:<segment name>')
])
class CreateHtmlWithGoodVersionsTask extends WatchmenTask {
	def logger = LogFactory.getLog(CreateHtmlWithGoodVersionsTask)
	private CommanderClient commanderClient
	private SegmentRegistry segmentRegistry
	private def ivyDependencies
	private def dynamicData;
	private def segmentIdentifier
	private File manualSegmentDirObject

	CreateHtmlWithGoodVersionsTask(Project project, commanderClient = new CommanderClient(), segmentRegistry = new SegmentRegistry()) {
		super(project)
		this.project = project
		this.commanderClient = commanderClient
		this.segmentRegistry = segmentRegistry
		this.ivyDependencies = getIvyDependencies()
		this.segmentIdentifier=project.segmentIdentifier
		this.dynamicData="<br/>"
		createDirectoryForFiles()
	}

	def execute() {
		validate()
		try {
			File htmlFile = createHtmlFile()
			def dependenciesHtml = buildDependenciesHtml()
			def htmlContent = buildHtmlPage(dependenciesHtml)
			writeToFile(htmlFile, htmlContent)
			writeJSFile()
			writeCSSFile()
			linkSelectionPageToThisJob(htmlFile.name)
		} catch (ignored) {
			logger.error(ignored.getCause(), ignored)
			logger.info(ignored.printStackTrace())
			logger.info("Unable to create HTML with good versions")
		}
	}

	def linkSelectionPageToThisJob(def file) {
		def linkUrl = "http://commander.phx.gapinc.dev/manualSegments/${file}"
		commanderClient.addLinkToUrl("Selection Page", linkUrl)
	}

	def getIvyDependencies() {
		def ecProperty = commanderClient.getECProperty("/myJob/ivyDependencies")
		def ecPropertyValue = ecProperty.getValue()
		def ivyDependencies = ecPropertyValue.split("\n")
		return ivyDependencies
	}

	def buildDependenciesHtml() {
		def dependenciesHtml = "<table border=\"0\">"
		ivyDependencies.each { dependency ->
			logger.info("dependency: " + dependency)
			def segmentId = segmentRegistry.getSegmentThatProducesIdentifier(dependency).toString()
			logger.info("segmentId of dependency $dependency is $segmentId")
			def goodVersionsDataJson = segmentRegistry.getSegmentRegistryPropertySheetData("goodVersions", segmentId)			
			dependenciesHtml += createTableRow(dependency, segmentId, goodVersionsDataJson)
		}
		dependenciesHtml += "</table><br/>"
		dependenciesHtml +=dynamicData
		return dependenciesHtml
	}

	def createTableRow(String dependency, String segmentId, goodVersionsJson) {
		def resolvedDependencies = [:]
		def versions = goodVersionsJson.propertySheet.property.propertyName.toArray()
		logger.info("versions of segment $segmentId is $versions")
		versions.each { version ->
			def dataGraph = goodVersionsJson.propertySheet.property.find{it.propertyName == version}
			def propData = dataGraph.propertySheet.property.find{it.propertyName == "resolvedDependencies"}
			resolvedDependencies.put(version, propData.value.toString())
		}
		def rowHtml = "<tr>\n<td>\n<label for=\"$segmentId\"> $segmentId </label>\n</td>\n"
		rowHtml += "<td><select id=\"$segmentId\" name=\"dependency\" onchange=\"showFields(this)\">"

		def latestVersion = Collections.max(Arrays.asList(versions))
		rowHtml += "<option value=\"$dependency:$latestVersion\">$latestVersion (latest)</option>\n"
		dynamicData += "<h4>Dependencies for the segment: $segmentId</h4><div id='$dependency:$latestVersion' style=\"display: block;\"><pre> ${resolvedDependencies[latestVersion]} </pre></div>\n"

        rowHtml+=populateVersionsExceptLatest(versions, latestVersion, dependency, resolvedDependencies)

		rowHtml += "</select>\n</td>\n</tr>"

		dynamicData = dynamicData.toString().replaceAll(~/------------------------------------------------------------/,"").
						replaceAll(~/default - Configuration for default artifacts./, "").
						replaceAll(~/Root project/,"").
						replaceAll(~/pipeline/,"").
						replaceAll(~/No dependencies/,"").
						replaceAll(~/\n *\n/,"\n")
		
		logger.debug("RowHtml : \n" + rowHtml)
		return rowHtml;
	}

    def populateVersionsExceptLatest(def versions, def latestVersion, String dependency, LinkedHashMap resolvedDependencies) {
        def rowHtmlTmp=''
        versions.each { version ->
            if (version != latestVersion) {
                rowHtmlTmp += "<option value=\"$dependency:$version\">$version</option>\n"
                dynamicData += "<div id='$dependency:$version' style=\"display: none;\"><pre> ${resolvedDependencies[version]} </pre></div>\n"
            }
        }
        return rowHtmlTmp
    }

    def generateTimeWithFormat() {
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddyyyy")

		int secondsFrom1970 = System.currentTimeMillis() / 1000

		def currentDateStamp = sdf1.format(new Date()).concat(secondsFrom1970.toString())

		logger.info("Calculate for current date like `date +%Y%m%d%G%s` format, output  is: $currentDateStamp")

		return currentDateStamp
	}

	def createDirectoryForFiles() {
		def shareHtdocsKey = "/server/watchmen_config/sharedHtdocs"

		def sharedHtdocs = commanderClient.getECProperty(shareHtdocsKey).getValue()

		String manualSegmentDir = sharedHtdocs + "/manualSegments"

		manualSegmentDirObject = new File(manualSegmentDir)

		try {
			manualSegmentDirObject.mkdirs()
			logger.info("Ensuring manualSegments directory is created: " + manualSegmentDirObject.absolutePath)
		}
		catch(IOException ioe) {
			logger.error(ioe.getCause(), ioe)
			throw ioe
		}

	}

	def createHtmlFile() {
		def dateWithFormat = generateTimeWithFormat()
		String fileName = segmentIdentifier + dateWithFormat + ".html"
		fileName = cleanSpacesAndColons(fileName)
		return createFileInManualSegmentDir(fileName)
	}

	def createFileInManualSegmentDir(String fileName) {
		File fileObject = new File(manualSegmentDirObject, fileName)
		try {
			fileObject.createNewFile()
			logger.info("Creating file : " + fileObject.getAbsoluteFile())
			return fileObject
		}
		catch (IOException ioe) {
			logger.error(ioe.getCause(), ioe)
			throw ioe
		}
	}

	def cleanSpacesAndColons(def fileName) {

		def newFileName = fileName.replaceAll("[\\s:]", "_")

		logger.info("converted FileName is : " + newFileName)

		return newFileName

	}

	def buildHtmlPage(String dependenciesHtml) {
		def projectName = commanderClient.getCurrentProjectName()

		def htmlContent = """
        <html>
        <head>
        <h1>$segmentIdentifier</h1>
    <script type="text/javascript" src="jquery.js"></script>
        <script type="text/javascript" src="manualSegment.js"></script>
    <link rel="stylesheet" type="text/css" href="manualSegment.css">
        <head>
        <body>
        <script type="text/javascript">
                function showFields(el)
        {
            for (i=0; i<el.options.length; i++) {
                option = el.options.item(i);
                divSel = document.getElementById(option.value);
                if(option.selected) {
                    divSel.style.display="block";
                }
                else {
                    divSel.style.display="none";
                }
            }
        }
        </script>
    <h2>Select Desired Versions of Segment Dependencies</h2>
                $dependenciesHtml
        <div id="submitDiv">
        <a href="" id='submit' projectName='$projectName' segmentIdentifier='$segmentIdentifier'><button>Go!</button></a>
        </div>
  <body>
</html>
    """
		logger.info("Created html file content is :\n" + htmlContent)
		return htmlContent
	}

	def writeToFile(File fileObject, String fileContent) {

		def writer = new FileWriter(fileObject)

		writer.write(fileContent)

		writer.close()

		logger.info("Contents written to " + fileObject.getAbsoluteFile())
	}

	def writeJSFile() {
		File jsFile = createFileInManualSegmentDir("manualSegment.js")

		def jsContent = """
        var manualSegment = {}

        manualSegment.view = function(){
          var self = {} ;

          self.submitLink = function(){
            return \$("#submit");
          };

          self.dependencies = function(){
            return \$("select[name='dependency']");
          };

          return self;
        };

        manualSegment.bindings = function(){
          var view = manualSegment.view();

          manualSegment.updateSubmitLink(view.dependencies(), view.submitLink());

          view.dependencies().each(function(){
            \$(this).change(function(){
              manualSegment.updateSubmitLink(view.dependencies(), view.submitLink());
          })});
        };

        manualSegment.updateSubmitLink = function(dependencies, submitLink){
          var values = [];
          for(var i=0; i < dependencies.size(); i++){
            values[i] = dependencies[i].value;
          };

          var versions = values.join(',');
          var projectName = submitLink.attr('projectName');
          var segmentIdentifier = submitLink.attr('segmentIdentifier');

          var url = "https://commander.phx.gapinc.dev/commander/runProcedure.php?runNow=1&projectName=" + escape(projectName) + "&procedureName=Kick+Off+Manual+Segment&numParameters=2&parameters1_name=segmentIdentifier&parameters1_value=" + escape(segmentIdentifier) + "&parameters2_name=selectedVersions&parameters2_value=" + escape(versions);

          submitLink.attr('href', url);
        }

        \$(document).ready(function(){
          manualSegment.bindings();
        });

        """

		writeToFile(jsFile, jsContent)

		logger.info("Javascript file written to " + jsFile.getAbsoluteFile())

		return jsFile
	}

	def writeCSSFile() {
		File cssFile = createFileInManualSegmentDir("manualSegment.css")

		def cssContent = """
        body {
            font-family: tahoma, arial, sans-serif;
            padding: 16px;
        }

        td {
           padding: 5px;
        }

        tr {
           background-color:#E9E9E9;
        }

        button {
           background-color: #E9E9E9;
           margin: 10px;
           font-weight: bold;
           float: left;
        }

        """

		writeToFile(cssFile, cssContent)

		logger.info("CSS file written to " + cssFile.getAbsoluteFile())

		return cssFile
	}
}
