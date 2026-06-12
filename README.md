# maven-browser

[![CI](https://github.com/aistomin/maven-browser/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/aistomin/maven-browser/actions/workflows/maven.yml)
[![Hits-of-Code](https://hitsofcode.com/github/aistomin/maven-browser)](https://hitsofcode.com/view/github/aistomin/maven-browser)
[![codecov](https://codecov.io/gh/aistomin/maven-browser/branch/master/graph/badge.svg)](https://codecov.io/gh/aistomin/maven-browser)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.aistomin/maven-browser)](https://central.sonatype.com/artifact/com.github.aistomin/maven-browser)
[![javadoc](https://javadoc.io/badge2/com.github.aistomin/maven-browser/javadoc.svg)](https://javadoc.io/doc/com.github.aistomin/maven-browser)

This Java library allows you to search and browse Maven Central. It uses the
[Maven Central Search API](https://search.maven.org/classic/#api) for artifact
lookup and the standard Maven repository layout at
[repo1.maven.org](https://repo1.maven.org/maven2/) (`maven-metadata.xml`) for
version information. The library can be useful for developers who create CI
scripts or other tools that need to read information from Maven Central.

## Getting Started

### System Requirements

- JDK 21 or higher.
- Apache Maven 3.3.9 or higher.

### Add Maven Dependency

If you use Maven, add the following configuration to your project's `pom.xml`:

```maven
<dependencies>
    <!-- other dependencies -->
    <dependency>
        <groupId>com.github.aistomin</groupId>
        <artifactId>maven-browser</artifactId>
        <version>5.0</version>
    </dependency>
    <!-- other dependencies -->
</dependencies>
```

or, if you use Gradle, add the following line to your build file:

```gradle
implementation 'com.github.aistomin:maven-browser:5.0'
```

### Read Data from Maven Central

We created some Groovy examples of how the library can be used. Please take a
look at the [examples here](https://github.com/aistomin/maven-browser/tree/master/examples).

## License

The project is licensed under the terms of the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

## Have You Found a Bug? Do You Have Any Suggestions?

Although we try our best, bugs are always possible :) We're also happy to hear
suggestions, ideas, and thoughts from you. Don't hesitate to
[create an issue](https://github.com/aistomin/maven-browser/issues/new).
It will help us make our project better. Thank you in advance!

## How to Contribute

Do you want to help us with the project? We would be happy to have your contribution!
Please fork the repository, make changes, and submit a pull request. We will review
your changes and merge them to the master branch if they meet our guidelines.
To avoid frustration, before sending us your pull request please run the full Maven build:

```
$ mvn clean install package javadoc:javadoc
```

Keep in mind our [system requirements](#system-requirements).
