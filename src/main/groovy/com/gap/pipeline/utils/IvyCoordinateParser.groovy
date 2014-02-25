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
}
