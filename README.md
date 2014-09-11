gap-gradle-plugin
=================

###[Electric Commander Project](https://commander.gapinc.dev/commander/link/projectDetails/projects/Watchmen%20Framework?objectId=project-592&filterName0=projectsPageSearch&filterDepth=1&tabGroup=proceduresHeader&s=Projects)

## Usage

Add to your Gradle script (e.g. build.gradle):

```groovy
buildscript {
    repositories {
        ivy {
            layout 'maven'
            url 'http://artifactory.gapinc.dev/artifactory/remote-repos'
        }
        maven {
            url 'http://artifactory.gapinc.dev/artifactory/remote-repos'
        }
    }
    dependencies {
      classpath 'com.gap:gap-gradle-plugin:+'
    }
}

// Then apply the plugins you want to use.
// Check (src/main/resources/META-INF/gradle-plugins/) to see a list of the plugins available.
apply plugin: 'gappipeline'
```
