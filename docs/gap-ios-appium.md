Gap iOS Appium
===============

This plugin supports to automatically start and stop Appium for running tests on simulator and real devices.

## Usage

```groovy
apply plugin: 'gap-ios-appium'

appiumConfig {
    extendedServerFlags = "--local-timezone"  // string of all extra parameters that we want to send to appium
    simulatorMode = false   // default is true so you dont need to specify if you just want to run on simulator
}

// Starts Appium before running the functional tests
runFunctionalTests.dependsOn startAppium
// Stops Appium after running the functional tests
runFunctionalTests.finalizedBy stopAppium
```

* Above `appiumConfig` is not mandatory if you want to run on simulator and no extra server arguments needed for Appium.
* Run task `startAppium` and this will start appium process and if `simulatorMode` set to false will also launch `ios-webkit-debug-proxy` as well.
* Run your functional tests either on simulator or device.
* Run task `stopAppium` after running your tests.

