package com.gap.pipeline.utils




class IvyCoordinateParser {

    def validate(String coordinates) {
        if (coordinates.trim().split(':').length != 3) {
            throw new IllegalArgumentException("The coordinates '${coordinates}' is of invalid format. The format should be <groupname>:<modulename>:<version>")
        }
    }

    def parse(String coordinates) {
        validate(coordinates)
        def parts = coordinates.split(':')
        [group: parts[0], name: parts[1], version: parts[2]]
    }

    def getAppName(String groupName) {
        return  groupName.substring(groupName.lastIndexOf(".")+1)
    }

    def getCookbookName(String groupName) {
        def groupSubstr = groupName.substring(0,groupName.lastIndexOf("."))
        return groupSubstr.substring(groupSubstr.lastIndexOf(".")+1)
    }

    def getCookbookVersion(String groupVersion) {
        return groupVersion.substring(0,groupVersion.lastIndexOf("."))
    }
}
