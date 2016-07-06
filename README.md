gap-gradle-plugin
=================

###[Electric Commander Project](https://commander.gapinc.dev/commander/link/projectDetails/projects/Watchmen%20Framework?objectId=project-592&filterName0=projectsPageSearch&filterDepth=1&tabGroup=proceduresHeader&s=Projects)

This plugin is split into multiple small plugins to improve performance and simplifying code.
## Will be making this plugin as wrapper plugin soon, meanwhile it's recommented to use separte plugins instead of this gaint plugin. 
Also any changes to this repo is not recommented, if at all youre making any changes make sure you update its correspodning plugin's repo code base also.

Here are the small repos:

http://github.gapinc.com/watchmen/gap-pipeline-plugin

http://github.gapinc.com/watchmen/gap-mobile-plugin

http://github.gapinc.com/watchmen/gap-miscellaneous-plugin

http://github.gapinc.com/watchmen/gap-deploy-plugin

http://github.gapinc.com/watchmen/gap-cookbook-plugin

http://github.gapinc.com/watchmen/gap-dbot-plugin

http://github.gapinc.com/watchmen/gap-artifactory-plugin

http://github.gapinc.com/watchmen/gap-sonar-plugin

http://github.gapinc.com/watchmen/gap-jruby-plugin

http://github.gapinc.com/watchmen/gap-gem-plugin


## Requirements
- Java 7
- Gradle 2.4

## Usage

Add to your Gradle script (e.g. build.gradle):

```groovy
buildscript {
    repositories {
        ivy {
            layout 'maven'
            url 'http://artifactory.gapinc.dev/artifactory/maven-repos'
        }
        maven {
            url 'http://artifactory.gapinc.dev/artifactory/maven-repos'
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

## Plugin documentation

* [Airwatch plugin](docs/airwatch-plugin.md)
* [CocoaPods plugin](docs/cocoapods.md)
* [DBot plugin](docs/dbot-plugin.md)
* [iOS Appium](docs/gap-ios-appium.md)
* [iOS Signing plugin](docs/gap-ios-signing.md)
* [iOS Sonar Runner plugin](docs/gap-ios-sonar-runner.md)
* [Xcode plugin](docs/gap-xcode-plugin.md)
