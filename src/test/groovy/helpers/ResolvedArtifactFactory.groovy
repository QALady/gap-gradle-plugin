package helpers

import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier
import org.gradle.api.internal.artifacts.DefaultResolvedArtifact
import org.gradle.api.internal.artifacts.ivyservice.dynamicversions.DefaultResolvedModuleVersion
import org.gradle.api.internal.artifacts.metadata.DefaultIvyArtifactName

class ResolvedArtifactFactory {
    static ResolvedArtifact resolvedArtifact(Map<String, ?> map) {
        map['groupId'] = map['groupId'] ?: ''
        map['type'] = map['type'] ?: ''
        map['name'] = map['name'] ?: ''
        map['ext'] = map['ext'] ?: ''
        map['classifier'] = map['classifier'] ?: ''
        map['file'] = map['file'] ?: new File("dummy")

        def moduleIdentifier = new DefaultModuleVersionIdentifier(map['groupId'], "", "")
        def artifactName = new DefaultIvyArtifactName(map['name'], map['type'], map['ext'], [classifier: map['classifier'], 'm:classifier': map['classifier']])
        new DefaultResolvedArtifact(new DefaultResolvedModuleVersion(moduleIdentifier), null, artifactName, new org.gradle.internal.Factory<File>() {
            @Override
            File create() {
                return map['file']
            }
        }, 0L)
    }
}
