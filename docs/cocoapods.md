CocoaPods plugin
===============

This plugin provides tasks used to publish internal Pods to [Gap CocoaPods repository](http://github.gapinc.dev/CocoaPods/Specs).

## Usage

Example content of `build.gradle`:
```groovy
apply plugin: "cocoapods"

podspec {
    name "MyPod"
    version "1.0.0"
    sourceLocation = "http://artifactory.gapinc.dev/artifactory/local-prod/my/group/MyPod.zip"
    // Or sourceLocation = "http://github.gapinc.dev/MyProject/MyPod"
}
```

Example content of `MyPod.podspec`:
```ruby
Pod::Spec.new do |s|
  s.name         = "@POD_NAME@"
  s.version      = "@POD_VERSION@"
  s.source       = { :http => "@POD_SOURCE_LOCATION@" }
  # Or s.source = { :git => "@POD_SOURCE_LOCATION@", :tag => s.version }
end
```

## Options
* `name` The name of the Pod to be published.
* `version` The version of the Pod to be published. Must follow semantic versioning ([reference](http://guides.cocoapods.org/syntax/podspec.html#version)).
* `sourceLocation` The location from where the library should be retrieved ([reference](http://guides.cocoapods.org/syntax/podspec.html#source)).

Each attribute in the `podspec` extension maps to a token. These tokens will be used to perform string replacement when the task `updatePodspec` is executed. Users should place these tolens where desired.

## Available tasks

* `updatePodspec` Recursively looks for a `*.podspec` file, starting in current directory, that matches the naming convention `"${podspec.name}.podspec"` (e.g. "MyPod.podspec", in example above) and performs the appropriate string replacements.
* `publishPod` (depends on `updatePodspec`) Publishes Pod to Gap's internal repository using `pod repo push` command ([reference](https://guides.cocoapods.org/making/private-cocoapods.html#-add-your-podspec-to-your-repo)).
