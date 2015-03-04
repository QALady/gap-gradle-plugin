package com.gap.gradle.airwatch

class ArtifactSpec {
    String groupId
    String name
    String classifier
    String type

    boolean hasAtLeastOneSpecDefined() {
        [groupId, name, classifier, type].any { it != null }
    }

    public String toStringWithDefinedValues() {
        def values = [:]
        values['groupId'] = groupId
        values['name'] = name
        values['classifier'] = classifier
        values['type'] = type

        def onlyDefinedValues = values.findAll { it.value != null }
        def keysAndValues = onlyDefinedValues.collect { "${it.key}='${it.value}'" }
        def description = keysAndValues.empty ? "is empty" : "{ " + keysAndValues.join(", ") + " }"

        return "artifact spec ${description}"
    }

    @Override
    public String toString() {
        return "ArtifactSpec{" +
                "groupId='" + groupId + '\'' +
                ", name='" + name + '\'' +
                ", classifier='" + classifier + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
