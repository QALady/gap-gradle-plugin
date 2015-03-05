Gap iOS Appium
===============

This plugin supports to automatically start and stop Appium for running tests on simulator and real devices.
## Usage

```groovy
apply plugin: 'gap-ios-appium'

appiumConfig{
    serverFlags = "--local-timezone"  // string of all extra parameters that we want to send to appium
    simulatorMode = false   // default is true you dont need to specify if you just want to run on simulator
}


```

* Above `appiumConfig` is not mandatory if you want to run on simulator and no extra server arguments needed for appium.
* Run task `startAppium` and this will start appium process and if `simulatorMode` set to true will also launch `ios-webkit-debug-proxy` as well.
* Run your functional tests either on simulator or device.
* Run task `stopAppium` after running your tests.
