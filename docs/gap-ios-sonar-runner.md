Gap iOS Sonar Runner
===============

This plugin provides some default configuration to run Sonar analysis against Objective-C code. Under the hood, this plugin just extends the current [gap-sonar-runner](http://github.gapinc.dev/watchmen/gap-gradle-plugin/blob/master/src/main/groovy/com/gap/gradle/plugins/GapSonarRunnerPlugin.groovy) functionality adding extra configuration. Swift code is currently not supported.

## Usage

```groovy
apply plugin: 'gap-ios-sonar-runner'

sonarRunner {
    sonarProperties {
        property 'sonar.projectName', 'MyApp analysed with Sonar' // The name of the project
        property 'sonar.sources', 'MyApp' // Path to source code directories (comma separated)
        property 'sonar.objectivec.testScheme', 'MyAppTests' // Scheme containing your tests
        property 'sonar.objectivec.appScheme', 'MyAppScheme' // Scheme to build and run your tests
    }
}
```

Before running the Sonar analysis, it is required that you run the following sequence of steps:

* Run task `extractSonarBuildWrapper` to download and extract the SonarSource build-wrapper, which is required for the analysis into folder `sonar/build-wrapper-3.2`.
* Run tasks `clean build` prefixed with the build-wrapper binary, e.g.: `sonar/build-wrapper-3.2/macosx-x86/build-wrapper-macosx-x86 --out-dir build/build_wrapper_output gradle clean build`.
* Clean some leftover processes from step above with `ps axwwwE | grep SQ_WRAPPER | awk '{ print $1 }' | xargs kill -9 | true ; ps axwwwE`.
* Optionally, run task `gcovAnalyze` (provided by [gap-xcode plugin](http://github.gapinc.dev/Ob1b7t6/gap-gradle-plugin/blob/master/docs/gap-xcode-plugin.md)) in case you want the Sonar analyzer to take the `gcov` results as additional input.
* Run task `sonar` to start the Sonar analysis.
