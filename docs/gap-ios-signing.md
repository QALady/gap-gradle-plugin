Gap iOS Signing
===============

This plugin is used to sign/resign an existing IPA package.

## Usage

```groovy
apply plugin: 'gap-ios-signing'

ipaSigning {
    signingIdentity signing.development # pre-configured "distribution" is also available

    artifact {
        groupId 'com.gap.apps.ios.example'
        name 'ExampleApp'
        classifier 'iphoneos'
        type 'ipa'
    }
}

task doSomethingWithSignedIpa(dependsOn: ipaSigning) << {
    println "Generated IPA file: ${ipaSigning.output}"
}
```

When you run `gradle doSomethingWithSignedIpa` the plugin will download the artifact in your `archives` configuration,
replace the embedded mobile provision profile and sign the IPA with the defined `signingIdentity`.

The new IPA will be available in the `ipaSigning.output` property.

## Options

* `signingIdentity` options are [documented here](gap-xcode-plugin.md#adding-custom-signing-identities)
* `artifact` options available:
  * `groupId`
  * `name`
  * `classifier`
  * `type`
