package com.gap.pipeline.ec

class SegmentRegistry {

    def log = LogFactory.getLog(com.gap.pipeline.ec.SegmentRegistry)
    def commander


    SegmentRegistry(commander = new CommanderClient()){
        this.commander = commander
    }

    void populate(ivyInfo) {
        def segmentConfig = commander.getCurrentSegmentConfig()
        def segment = commander.getCurrentSegment()

        log.info("populating segment registry for current segment ${segment}")

        setSegmentRegistryValue(segment, 'ivyIdentifiers', ivyInfo.identifiers().join('\n'))
        setSegmentRegistryValue(segment, 'ivyDependencies', ivyInfo.dependencies().join('\n'))
        setSegmentRegistryValue(segment, 'svnUrl', segmentConfig.scmUrl)
        setSegmentRegistryValue(segment, 'workingDir', segmentConfig.workingDir)
        setSegmentRegistryValue(segment, 'ciDir', segmentConfig.ciDir)
        setSegmentRegistryValue(segment, 'gradleFile', segmentConfig.gradleFile)

        ivyInfo.identifiers().each{ ivyId ->
            setIdentifierRegistryValue(ivyId, segment)
        }
    }

    void registerWithUpstreamSegments(def ivyInfo) {
        def currentSegment = commander.getCurrentSegment()

        log.info("registering current segment \"${currentSegment}\" with upstream segments")

        def upstreamSegments = []
        ivyInfo.dependencies().each { upstreamId ->
            if(identifierIsProducedByAnotherSegment(upstreamId, currentSegment)){
                def upstreamSegment = getSegmentThatProducesIdentifier(upstreamId)
                registerWithUpstreamSegment(upstreamSegment, currentSegment)
                upstreamSegments.add(upstreamSegment)
            }
        }
        unregisterFromRemovedUpstreamSegments(currentSegment, ivyInfo)
        setSegmentRegistryValue(currentSegment, "upstreamSegments", upstreamSegments.join("\n"))
    }

    private def unregisterFromRemovedUpstreamSegments(def currentSegment, def newUpstreamSegments) {
        def priorUpstreamSegments = getSegmentRegistryValue(currentSegment, "upstreamSegments").split("\n") as Set
        priorUpstreamSegments.remove("")
        priorUpstreamSegments.removeAll(newUpstreamSegments)
        priorUpstreamSegments.each { upstreamSegment ->
            unregisterFromUpstreamSegment(upstreamSegment, currentSegment)
        }
    }

    private void registerWithUpstreamSegment(upstreamSegment, currentSegment) {
        log.info("adding \"${currentSegment}\" to registered downstream segments of \"${upstreamSegment}\"")

        def downstreamSegments = getSegmentRegistryValue(upstreamSegment, 'downstreamSegments').toString().split("\n") as Set
        downstreamSegments.add(currentSegment.toString())
        downstreamSegments.remove("")
        setSegmentRegistryValue(upstreamSegment, 'downstreamSegments', downstreamSegments.join("\n"))
    }

    private void unregisterFromUpstreamSegment(upstreamSegment, currentSegment) {
        log.info("removing \"${currentSegment}\" from registered downstream segments of \"${upstreamSegment}\"")

        def downstreamSegments = getSegmentRegistryValue(upstreamSegment, 'downstreamSegments').toString().split("\n") as Set
        downstreamSegments.remove(currentSegment.toString())
        setSegmentRegistryValue(upstreamSegment, 'downstreamSegments', downstreamSegments.join("\n"))
    }

    private def getSegmentThatProducesIdentifier(identifier) {
        def segmentId = commander.getECProperty("/projects[WM Segment Registry]/IdentifierRegistry/${identifier}/segment").value
        Segment.fromString(segmentId)
    }

    private def identifierIsProducedByAnotherSegment(identifier, currentSegment) {
        def segmentNameProperty = commander.getECProperty("/projects[WM Segment Registry]/IdentifierRegistry/${identifier}/segment")
        segmentNameProperty.isValid() && segmentNameProperty.value != currentSegment.toString()
    }

    private void setSegmentRegistryValue(segment, key, value){
        log.info("setting /projects[WM Segment Registry]/SegmentRegistry/${segment}/${key} to ${value}")
        commander.setECProperty("/projects[WM Segment Registry]/SegmentRegistry/${segment}/${key}", value)
    }

    private def getSegmentRegistryValue(segment, key){
        commander.getECProperty("/projects[WM Segment Registry]/SegmentRegistry/${segment}/${key}").value
    }

    private def setIdentifierRegistryValue(def identifier, def segment) {
        log.info("/projects[WM Segment Registry]/IdentifierRegistry/${identifier}/segment to ${segment}")
        commander.setECProperty("/projects[WM Segment Registry]/IdentifierRegistry/${identifier}/segment", segment.toString())
    }

}

import org.apache.commons.logging.LogFactory
