Dbot plugin
===============

## Usage

```groovy
apply plugin: 'dbot'

buildscript {
  repositories {
    add(new org.apache.ivy.plugins.resolver.URLResolver()) {
      name = 'GID Repo'
        validate = false
        addIvyPattern("http://nfs01.sf.gid.gap.com/build_artifacts/[organisation]/[module]/ivy-[revision].xml")
        addArtifactPattern("http://nfs01.sf.gid.gap.com/build_artifacts/[organisation]/[module]/[revision]/[type]/[artifact]-[revision].[ext]")
        addArtifactPattern("http://nfs01.sf.gid.gap.com/build_artifacts/[organisation]/[module]/[revision]/[artifact]-[revision].[ext]")
        addArtifactPattern("http://nfs01.sf.gid.gap.com/build_artifacts/[organisation]/[module]/[revision]/[organisation]-[artifact]-[revision].[ext]")
    }
    ivy {
      layout 'maven'
      url 'http://artifactory.gapinc.dev/artifactory/maven-repos'
    }
    maven {
      url 'http://artifactory.gapinc.dev/artifactory/maven-repos'
    }
    maven {
      url "http://nfs01.sf.gid.gap.com/build_artifacts"
    }
    ivy {
      layout "maven"
      url "http://nfs01.sf.gid.gap.com/build_artifacts"
    }
    mavenCentral()
  }

  dependencies {
    classpath 'com.gap:gap-gradle-plugin:+'
    classpath 'net.saliman:gradle-liquibase-plugin:1.0.0'
    classpath 'oracle:ojdbc6:11.2.0.2'
  }
}

liquibase {
  activities {
    main {
      changeLogFile 'changelog.groovy'
      url 'any url'
      username 'username'
      password 'password'
    }
  }

  runList = { project.ext.runList }
}

//dbot config
repositories {
  add project.repositories.dbot_repo
}
configurations {
  runtime
}

dependencies {
  runtime group: 'gap', configuration: 'compile(*)', name: 'dbot', version: '0.0.+', type: 'jar'
  runtime(group: 'gap', configuration: 'runtime', name: 'dbot', version: '0.0.+', type: 'jar') {
    transitive = false
  }
}
```

## Options
* `url` specifies the database url connection
* `username` specifies the username
* `password` specifies the password
* `schema` specifies the schema
* `driver` especifies the driver to be used i.e. oracle.jdbc.driver.OracleDriver
* `flavor` especifies the database flavor (oracle, postgres, mysql, etc)


## Tasks available

* all liquibase tasks [here](https://github.com/tlberglund/gradle-liquibase-plugin)
* `generateChangeLogDBOT`: extract all database objects (procedures, functions, triggers, etc)


