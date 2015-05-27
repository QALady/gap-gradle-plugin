Airwatch plugin
===============

This plugin requires REST API access enabled in the AirWatch instance. Check how to enable it in this [wiki page](http://github.gapinc.dev/watchmen/gap-gradle-plugin/wiki/Enabling-REST-API-access-in-AirWatch).

## Usage

```groovy
apply plugin: 'airwatch'

configurations {
    archives
}

dependencies {
    archives group: 'com.gap.something', name: 'foobar', version: '30806', configuration: 'archives'
}

airwatchUpload {
  appName 'My App Name'
  appDescription 'Some nice description about it'
  configFilename 'production.yml'
  pushMode 'auto'
  uploadChunks 25

  smartGroups 'group1,group2'

  artifact {
    groupId 'com.gap.something'
    name 'foobar'
    classifier 'iphoneos'
    type 'ipa'
  }

  searchParamsToRetireApp {
    bundleId 'com.gap.search.toretire'
    status 'Active'
  }

  targetEnvironment environments.preProduction
}
```

## Options

* `appName` specifies the application name that will be created in Airwatch
* `appDescription` specifies the application description in Airwatch
* `configFilename` specifies the file name from the "airwatchConfig" artifact that will be used for the configuration of the app in Airwatch
* `pushMode` should be "auto" or "ondemand"
* `uploadChunks` specifies in how many chunks the ipa is going to be uploaded. If omitted, the default value is used (25).
* `environments` allows the addition of other Airwatch environments that you might want to push artifacts to. There are pre-configured ones: preProduction (aka CN11) and production.
* `smartGroups` used to configure which Smart Groups are going to be automatically assigned to the ipa
* `searchParamsToRetireApp` used to define criteria to search for appId with the parameters provided so that autoRetireAppPreviousVersion task can use this appId and retire the application in Airwatch.
* `ipaFile` specifies a `File` reference to the IPA that will be pushed to Airwatch. This option overrides the `artifact` option. You can use it together with the [gap-ios-signing](gap-ios-signing.md) plugin, for example:

  ```groovy
  ipaSigning {
    // Other options here...
  }

  airwatchUpload {
    // Other options here...
    ipaFile { ipaSigning.output }
  }

  pushArtifactToAirWatch.dependsOn ipaSigning
  ```

* `artifact` specifies which artifact from the "archives" configuration will be uploaded to Airwatch. You can see this as a criteria filter, you can specify some or all of the artifact's attributes to narrow down the artifact that you want to upload. Matching only using classifier, will grab the first artifact found in archives with that classifier:

  ```groovy
  airwatchUpload {
    artifact {
      classifier 'iphoneos'
    }
  }
  ```

* `targetEnvironment` specifies to which Airwatch instance the artifact will be uploaded

## Adding custom Environments

You can also add a custom one or overwrite. e.g.: Adding a new environment called "myEnv":

  ```groovy
  airwatchUpload {
    environments {
      myEnv {
        apiHost 'http://example.com'
        credentialName 'credential-name'
        locationGroupId '123'
        // ...
      }
    }
  }
  ```

And you can overwrite pre-configured values for existing ones. e.g.: Override values for "preProduction":

  ```groovy
  airwatchUpload {
    environments {
      preProduction {
        credentialName 'another-credential-name'
        locationGroupId 'new-value'
      }
    }
  }
  ```

### Environment attributes
* `apiHost`: the URL for the Airwatch REST instance
* `consoleHost`: the URL for the Airwatch Console (Web interface) -- used to configure the application configuration variables
* `tenantCode`: the Airwatch tenant code
* `locationGroupId`: defines under which Airwatch location group you'll create the application
* `credentialName`: the name of the credential stored in the [credentials](http://github.gapinc.dev/mpl/credentials) repository

### searchParamsToRetireApp

Specify only the parameters needed to uniquely identify and retire an application. Not all the parameters are needed. Start with bundleId and status parameters to identify an application to retire

* `bundleId`: bundleId of the current build
* `applicationName`: Display name of the application
* `status`: status of the application

## Tasks available

* `pushArtifactToAirWatch`: uses Airwatch REST API to upload the artifact.
* `autoAssignSmartGroups`: uses Airwatch REST API to assign the configured `smartGroups` to the uploaded ipa. It depends on `pushArtifactToAirWatch`.
* `autoRetireAppPreviousVersion`: This will retire the application by appId provided using airwath Rest api. It depends on `pushArtifactToAirWatch` to search for a give appId that meets the criteria.
* `configureApp`: goes through the Airwatch Console and configures the uploaded application with the values from the "airwatchConfig" config file. It depends on `pushArtifactToAirWatch`.
