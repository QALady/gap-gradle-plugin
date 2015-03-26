Gap iOS Appium
===============

This plugin supports to automatically start and stop Appium for running tests on simulator and real devices.

## Usage

```groovy
apply plugin: "gap-ios-appium"

appiumConfig {
    simulatorMode = false
    extendedServerFlags = "--local-timezone"
    instrumentsTemplateURI = "file:ci/path/to/file.tracetemplate" // or "http://example.com/my-template.tracetemplate"
}

// Starts Appium before running the functional tests
runFunctionalTests.dependsOn startAppium

// Stops Appium after running the functional tests
runFunctionalTests.finalizedBy stopAppium
```

* Above `appiumConfig` is not mandatory if you want to run on simulator and no extra server arguments needed for Appium.
* Run task `startAppium` to spawn an Appium server in background. If `simulatorMode` is **false**, [ios-webkit-debug-proxy](https://github.com/google/ios-webkit-debug-proxy) will also be launched in background.
* Run your functional tests.
* Run task `stopAppium` to stop the server.

## Options
* `simulatorMode` defaults to **true**, so you don't need to specify it if you just want to run on the iOS Simulator. To disable simulator mode you need a real device attached to the machine. Furthermore, in real device mode, a custom [Instruments trace template](https://developer.apple.com/library/mac/documentation/DeveloperTools/Conceptual/InstrumentsUserGuide/InstrumentsQuickStart/InstrumentsQuickStart.html) will be used.
* `instrumentsTemplateURI` is an optional parameter to specify what template will be used with Instruments. By default, [this one](http://github.gapinc.dev/mpl/instruments-standard-template) will be used.
* `extendedServerFlags` can be used to specify extra parameters to be used with Appium.

## Default Appium arguments

By default, Appium will start with the following flags:
* `--session-override`
* `--log-no-colors`
* `--log-timestamp`
* `--udid` (only when `simulatorMode` is **false**)
* `--tracetemplate` (only when `simulatorMode` is **false**)
* `--trace-dir` (only when `simulatorMode` is **false**)

For a description of these and all other available flags, please check [Appium documentation](http://appium.io/slate/en/master/?ruby#server-flags).

## Test results

* All Appium log will be written to `build/test/logs/appium.log`.
* Instruments trace results will be stored at `build/test/instruments-trace-results` folder.

## Available tasks

* `startAppium`
* `startiOSWebkitDebugProxy` (automatically called before `startAppium` runs)
* `stopAppium` (stops both Appium and ios-webkit-debug-proxy)
