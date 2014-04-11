package com.gap.pipeline.ec

class Segment {

    def projectName
    def procedureName

    Segment(projectName, procedureName) {
        this.projectName = projectName
        this.procedureName = procedureName
    }


    static Segment fromString(String segmentId) {
        def parts = segmentId.split(":")
        return new Segment(parts[0], parts[1])
    }

    def getProjectName() {
        return projectName
    }

    def getProcedureName() {
        return procedureName
    }

    @Override
    def String toString(){
        return "${projectName}:${procedureName}".toString()
    }
}
