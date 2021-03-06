Gap Xcode plugin
===============

This plugin exposes an extension called `xcode` that provides some syntax sugar the [openbakery's Xcode plugin](https://github.com/openbakery/gradle-xcodePlugin).

## Usage

```groovy
buildscript {
    repositories {
        maven { url 'http://artifactory.gapinc.dev/artifactory/maven-repos' }
        maven { url 'http://artifactory.gapinc.dev/artifactory/local-non-prod' }
        ivy {
            layout 'maven'
            url "http://artifactory.gapinc.dev/artifactory/maven-repos"
        }
    }

    dependencies {
        classpath group: 'com.gap', name: 'gap-gradle-plugin', version: '+'
    }
}

apply plugin: 'gap-xcode'

xcode {
    workspace = 'MyApp.xcworkspace'
    scheme = 'SomeScheme'

    test {
        scheme 'MyScheme' // Will use 'MyScheme' for testing instead of 'SomeScheme'

        destination {
            platform = 'iOS Simulator'
            name = 'iPhone 6'
            os = '8.1'
        }
    }

    build {
        productName 'My Application' // no need to specify if your target name is same as the product name
        configuration 'Debug'      // default is set to 'Release' only specify if it is not 'Release'
        target 'MyApp' // you can also pass a closure, like in the line below
        sdk { 'iphoneos' }
        signingIdentity { signing.distribution } // possible arguments: signing.distribution, signing.development, "distribution", "development"
    }

    archive {
        version = '1.0.0'
        shortVersionString = '1.0.0'
        scmRevision = '7bf2862' // Git or SVN revision that produced the current version
    }
}
```

## Options

In addition to this documentation, please use the official [xcodebuild documentation](https://developer.apple.com/library/mac/documentation/Darwin/Reference/ManPages/man1/xcodebuild.1.html) as reference.

* `workspace` (String or Closure) specifies the workspace to be used.
* `scheme` (String or Closure) specifies which scheme will be used.

### Test options

* `scheme` (String or Closure) specifies which scheme will be used for testing. If not specified the `xcode.scheme` will be used.
* `destination.platform` (String or Closure) specifies in which platform the unit tests will run, usually the simulator.
* `destination.name` (String or Closure) specifies the name of the device to use, e.g.: `iPhone 5s`, `iPhone 6`, etc.
* `destination.os` (String or Closure) specifies the iOS version of the simulator, e.g.: `7.1`, `8.1`, etc.

### Build options

* `productName` (String or Closure) needs to be the Product Name that your Xcode project uses for your `target`. If not specified the value of `target` will be used.
* `target` (String or Closure) specifies which target will be built. If not specified the value of `xcode.scheme` will be used. See available targets with `xcodebuild -list`.
* `sdk` (String or Closure) specifies which SDK will be used to build the target, e.g.: `iphoneos`, `iphonesimulator`. See all available SDKs with `xcodebuild -showsdks`.
* `signingIdentity` (SigningIdentity or Closure) specifies which Code Signing Identity will be used to sign the app. There are two pre-configured identities available: `signing.development` and `signing.distribution`. You can also only specify the name of an existing SigningIdentity.

### Archive options

* `version` (String or Closure) is equivalent to [CFBundleVersion](https://developer.apple.com/library/ios/documentation/General/Reference/InfoPlistKeyReference/Articles/CoreFoundationKeys.html#//apple_ref/doc/uid/20001431-102364). Will be used to set artifact version uploaded to Artifactory.
* `shortVersionString` (String or Closure) is equivalent to [CFBundleShortVersionString](https://developer.apple.com/library/ios/documentation/General/Reference/InfoPlistKeyReference/Articles/CoreFoundationKeys.html#//apple_ref/doc/uid/20001431-111349).
* `scmRevision` (String or Closure) is an optional parameter to set what SVN/Git revision generated that ipa version.

## Adding custom Signing Identities

It is possible add new custom signing identities, e.g.: Adding an identity called "myIdentity":

```groovy
xcode {
    signing {
        myIdentity {
            description 'iPhone Developer: My Name (XXXXXX)'
            certificateURI 'http://example.com/Development.p12' // Both "http://" and "file://" urls are allowed
            certificatePassword 'secret'
            mobileProvisionURI 'file://path/to/Development.mobileprovision' // Both "http://" and "file://" urls are allowed
        }
    }
}
```

It is also possible to overwrite pre-configured values for existing identities, e.g.: Override values for the existing `development` identity:

```groovy
xcode {
    signing {
        development {
            certificateURI 'http://example.com/AnotherCertificate.p12' // Both http:// and file:// urls are allowed
            certificatePassword 'another-secret'
        }
    }
}
```

## Displaying version and SVN/Git revision in the Settings app

To display the `archive.version` and `archive.scmRevision` in iOS Settings app, you need to create the proper entries in the `Root.plist` of you app (inside `Settings.bundle`) and use the tokens `@version@` and `@scmRevision@` as the `DefaultValue`. Then, after the app is built, the plugin will replace the tokens with the values specified in `archive`.

e.g.:

```xml
<dict>
    <key>Type</key>
    <string>PSTitleValueSpecifier</string>
    <key>Title</key>
    <string>Version</string>
    <key>Key</key>
    <string>version</string>
    <key>DefaultValue</key>
    <string>@version@</string>
</dict>
<dict>
    <key>Type</key>
    <string>PSTitleValueSpecifier</string>
    <key>Title</key>
    <string>SCM Revision</string>
    <key>Key</key>
    <string>scmRevision</string>
    <key>DefaultValue</key>
    <string>@scmRevision@</string>
</dict>
```

## Tasks available

* All [openbakery's Xcode plugin](https://github.com/openbakery/gradle-xcodePlugin) tasks, e.g.: `build`, `test`, `archive`.
* `gcovCoverage`: Runs [gcov](https://gcc.gnu.org/onlinedocs/gcc/Gcov.html) test code coverage analysis.
