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

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Segment segment = (Segment) o

        if (procedureName != segment.procedureName) return false
        if (projectName != segment.projectName) return false

        return true
    }

    int hashCode() {
        int result
        result = (projectName != null ? projectName.hashCode() : 0)
        result = 31 * result + (procedureName != null ? procedureName.hashCode() : 0)
        return result
    }
}
