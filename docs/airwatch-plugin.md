Airwatch plugin
===============

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

  artifact {
    groupId 'com.gap.something'
    name 'foobar'
    classifier 'iphoneos'
    type 'ipa'
  }

  targetEnvironment environments.preProduction
}
```


## Options

* `appName` specifies the application name that will be created in Airwatch
* `appDescription` specifies the application description in Airwatch
* `configFilename` specifies the file name from the "airwatchConfig" artifact that will be used for the configuration of the app in Airwatch
* `pushMode` should be "auto" or "ondemand"
* `environments` allows the addition of other Airwatch environments that you might want to push artifacts to. There are pre-configured ones: preProduction (aka CN11) and production. You can also add a custom one or overwrite.
   * Adding a new one, called "myEnv":

   ```groovy
   airwatchUpload {
     environments {
       myEnv {
         apiHost 'http://example.com'
         credentialName 'credential-name-in-EC'
         locationGroupId '123'
         // ...
       }
     }
   }
   ```
   * Overwriting pre-configured values for "preProduction":

    ```groovy
    airwatchUpload {
      environments {
        preProduction {
          credentialName 'some-other-value'
          locationGroupId 'new-value'
        }
      }
    }
    ```

    * Environment attributes
        * `apiHost`: the URL for the Airwatch REST instance
        * `consoleHost`: the URL for the Airwatch Console (Web interface) -- used to configure the application configuration variables
        * `tenantCode`: the Airwatch tenant code
        * `locationGroupId`: defines under which Airwatch location group you'll create the application
        * `credentialName`: this is the name of the credential in EletricCommander (project [WM Credentials](https://commander.gapinc.dev/commander/link/projectDetails/projects/WM%20Credentials?objectId=project-85a02ed1-42a2-11e4-a559-00505625f614&filterName0=projectsPageSearch&filterDepth=1&tabGroup=credentialsHeader&s=Projects)) that knows the actual username/password to access Airwatch

* `artifact` specifies which artifact from the "archives" configuration will be uploaded to Airwatch. You can see this as a criteria filter, you can specify some or all of the artifact's attributes to narrow down the artifact that you want to upload.
    * Matching only using classifier, will grab the first artifact found in archives with that classifier:

    ```groovy
    airwatchUpload {
      artifact {
        classifier 'iphoneos'
      }
    }
    ```
* `targetEnvironment` specifies to which Airwatch instance the artifact will be uploaded

## Tasks available

* `pushArtifactToAirWatch`: uses Airwatch REST API to upload the artifact.
* `configureApp`: goes through the Airwatch Console and configures the uploaded application with the values from the "airwatchConfig" config file. It depends on `pushArtifactToAirWatch`.
