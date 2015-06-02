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
    trustStoreFileURI = "file:ci/path/to/custom/TrustStore.sqlite3" // or "http://example.com/TrustStore.sqlite3"
}

// Starts Appium before running the functional tests
runFunctionalTests.dependsOn startAppium

// Stops Appium after running the functional tests
runFunctionalTests.finalizedBy stopAppium
```

* Above `appiumConfig` is not mandatory if you want to run on simulator and no extra server arguments needed for Appium.
* Run task `startAppium` to spawn an Appium server in background. If `simulatorMode` is **false**, [ios-webkit-debug-proxy](https://github.com/google/ios-webkit-debug-proxy) (required for tests against real devices) will also be launched in background.
* Run your functional tests.
* Run task `stopAppium` to stop the server.

## Options
* `simulatorMode` defaults to **true**, so you don't need to specify it if you just want to run on the iOS Simulator. To disable simulator mode you need a real device attached to the machine. Furthermore, in real device mode, a custom [Instruments trace template](https://developer.apple.com/library/mac/documentation/DeveloperTools/Conceptual/InstrumentsUserGuide/InstrumentsQuickStart/InstrumentsQuickStart.html) will be used.
* `instrumentsTemplateURI` is an optional parameter to specify what template will be used with Instruments. By default, [this one](http://github.gapinc.dev/mpl/instruments-standard-template) will be used.
* `extendedServerFlags` can be used to specify extra parameters to be used with Appium.
* `trustStoreFileURI` can be used to point to a custom `TrustStore.sqlite3` file (see CA certificates session below).

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

## CA certificates in Simulators

Some apps may require that you import self-signed CA certificates into the simulator. You can use the [ADVTrustStore](https://github.com/ADVTOOLS/ADVTrustStore) tool to generate a custom `TrustStore.sqlite3` file and copy it into the iOS Simulator using the `importTrustStoreIntoSimulators` task.

## Available tasks

* `startAppium` spawns an Appium server in the background (using `extendedServerFlags` if set), along with ios-webkit-debug-proxy if `simulatorMode` is enabled.
* `startiOSWebkitDebugProxy` automatically called before `startAppium` runs.
* `stopAppium` stops both Appium and ios-webkit-debug-proxy.
* `importTrustStoreIntoSimulators` copies TrustStore file specified by `trustStoreFileURI` into simulators folders.
