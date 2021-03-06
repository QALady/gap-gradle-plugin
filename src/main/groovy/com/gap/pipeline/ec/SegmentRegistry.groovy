package com.gap.pipeline.ec

import org.slf4j.LoggerFactory

class SegmentRegistry {

    def logger = LoggerFactory.getLogger(com.gap.pipeline.ec.SegmentRegistry)
    CommanderClient commander
    def segmentRegistryPath = "/projects[WM Segment Registry]/SegmentRegistry"
    def identifierRegistryPath = "/projects[WM Segment Registry]/IdentifierRegistry"


    SegmentRegistry(commander = new CommanderClient()){
        this.commander = commander
    }

    void populate(ivyInfo) {
        def segmentConfig = commander.getCurrentSegmentConfig()
        def segment = commander.getCurrentSegment()

        logger.info("populating segment registry for current segment ${segment}")

        setSegmentRegistryValue(segment, 'ivyIdentifiers', ivyInfo.identifiers().join('\n'))
        setSegmentRegistryValue(segment, 'ivyDependencies', ivyInfo.dependencies().join('\n'))
        setSegmentRegistryValue(segment, 'svnUrl', segmentConfig.scmUrl)
        setSegmentRegistryValue(segment, 'workingDir', segmentConfig.workingDir)
        setSegmentRegistryValue(segment, 'gradleFile', segmentConfig.gradleFile)
        setSegmentRegistryValue(segment, 'scmConfigName', segmentConfig.scmConfigName)
        setSegmentRegistryValue(segment, 'isManual', segmentConfig.isManual.toString())

        ivyInfo.identifiers().each{ ivyId ->
            setIdentifierRegistryValue(ivyId, segment)
            if(ivyId == 'resolvedDependencies'){
                commander.setECProperty("resolvedDependencies", segment)
            }
        }
    }

    void registerWithUpstreamSegments(def ivyInfo) {
        def currentSegment = commander.getCurrentSegment()

        logger.info("registering current segment \"${currentSegment}\" with upstream segments")

        def upstreamSegments = []
        ivyInfo.dependencies().each { upstreamId ->
            if(identifierIsProducedByAnotherSegment(upstreamId, currentSegment)){
                def upstreamSegment = getSegmentThatProducesIdentifier(upstreamId)
                registerWithUpstreamSegment(upstreamSegment, currentSegment)
                upstreamSegments.add(upstreamSegment)
            }
        }
        unregisterFromRemovedUpstreamSegments(currentSegment, upstreamSegments)
        setSegmentRegistryValue(currentSegment, "upstreamSegments", upstreamSegments.join("\n"))
    }

    private def unregisterFromRemovedUpstreamSegments(def currentSegment, def newUpstreamSegments) {
        def priorUpstreamSegmentsProperty = getSegmentRegistryProperty(currentSegment, "upstreamSegments")
        if(priorUpstreamSegmentsProperty.isValid()){
            def priorUpstreamSegments = getSegmentsFromString(priorUpstreamSegmentsProperty.value)
            priorUpstreamSegments.remove("")
            priorUpstreamSegments.removeAll(newUpstreamSegments)
            priorUpstreamSegments.each { upstreamSegment ->
                unregisterFromUpstreamSegment(upstreamSegment, currentSegment)
            }
        }
    }

    def getSegmentsFromString(def stringWithNewLines) {
        def segments = []
        stringWithNewLines.split('\n').each {segmentId ->
            if(!segmentId.equals("")){
                segments.add(Segment.fromString(segmentId))
            }
        }
        return segments as Set
    }

    private void registerWithUpstreamSegment(upstreamSegment, currentSegment) {
        logger.info("adding \"${currentSegment}\" to registered downstream segments of \"${upstreamSegment}\"")

        def downstreamSegmentsProperty = getSegmentRegistryProperty(upstreamSegment, 'downstreamSegments')
        def downstreamSegments = [] as Set
        if(downstreamSegmentsProperty.isValid()){
            downstreamSegments = downstreamSegmentsProperty.value.toString().split("\n") as Set
        }

        downstreamSegments.add(currentSegment.toString())
        downstreamSegments.remove("")
        setSegmentRegistryValue(upstreamSegment, 'downstreamSegments', downstreamSegments.join("\n"))
    }

    private void unregisterFromUpstreamSegment(upstreamSegment, currentSegment) {
        logger.info("removing \"${currentSegment}\" from registered downstream segments of \"${upstreamSegment}\"")

        def downstreamSegmentsProperty = getSegmentRegistryProperty(upstreamSegment, 'downstreamSegments')
        if(downstreamSegmentsProperty.isValid()){
            def downstreamSegments =downstreamSegmentsProperty.value.toString().split("\n") as Set
            downstreamSegments.remove(currentSegment.toString())
            setSegmentRegistryValue(upstreamSegment, 'downstreamSegments', downstreamSegments.join("\n"))
        }
    }

    public def getSegmentThatProducesIdentifier(identifier) {
        def segmentId = commander.getECProperty("/projects[WM Segment Registry]/IdentifierRegistry/${identifier}/segment").value
        Segment.fromString(segmentId)
    }

    private def identifierIsProducedByAnotherSegment(identifier, currentSegment) {
        def segmentNameProperty = commander.getECProperty("/projects[WM Segment Registry]/IdentifierRegistry/${identifier}/segment")
        segmentNameProperty.isValid() && segmentNameProperty.value != currentSegment.toString()
    }

    private void setSegmentRegistryValue(segment, key, value){
        logger.info("setting /projects[WM Segment Registry]/SegmentRegistry/${segment}/${key} to ${value}")
        commander.setECProperty("/projects[WM Segment Registry]/SegmentRegistry/${segment}/${key}", value)
    }

    public def getSegmentRegistryValue(segment, key){
        commander.getECProperty("/projects[WM Segment Registry]/SegmentRegistry/${segment}/${key}").value
    }

    private def getSegmentRegistryProperty(segment, key){
        commander.getECProperty("/projects[WM Segment Registry]/SegmentRegistry/${segment}/${key}")
    }

    private def setIdentifierRegistryValue(def identifier, def segment) {
        logger.info("/projects[WM Segment Registry]/IdentifierRegistry/${identifier}/segment to ${segment}")
        commander.setECProperty("/projects[WM Segment Registry]/IdentifierRegistry/${identifier}/segment", segment.toString())
    }


    public def getIdentifierRegistryValue(segment, key){
        commander.getECProperty("${identifierRegistryPath}/${segment}/${key}").value
    }

    public def getSegmentRegistryPropertySheetData(propertSheetName, segmentId) {
         def propertiesPath = "${segmentRegistryPath}/${segmentId}/${propertSheetName}"
         return commander.getECProperties([path: propertiesPath, recurse: 1])
    }
}
