# maven-browser
[![Build Status](https://travis-ci.org/aistomin/maven-browser.svg?branch=master)](https://travis-ci.org/aistomin/maven-browser)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.aistomin/maven-browser/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.aistomin/maven-browser)

This Java library allows to browse the Maven Central repository. It uses 
[Maven Central API](https://search.maven.org/classic/#api) to retrieve the data from the
repository. The tool can be useful for developers who create a CI scripts or other 
environmental software that needs to read information from Maven repository.

## Getting Started

### System Requirements
 - JDK 8 or higher.
 - Apache Maven 3.3.9 or higher
 
### Add Maven Dependency
If you use Maven, add the following configuration to your project's `pom.xml`
```maven
<dependencies>
    <!-- other dependencies are there -->
    <dependency>
        <groupId>com.github.aistomin</groupId>
        <artifactId>maven-browser</artifactId>
        <version>1.0</version>
    </dependency>
    <!-- other dependencies are there -->
</dependencies>
```
or, if you use `Gradle`, add the following line to your build file:
```
compile 'com.github.aistomin:maven-browser:1.0'
```

### Read the Data from Maven Repository
We created some `Groovy` examples of how the library can be used. Please take a
 look at the [examples here](https://github.com/aistomin/maven-browser/tree/master/examples).
 
## Licence
The project is licensed under the terms of the 
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

## Have You Found a Bug? Do You Have Any Suggestions?
Although we try our best, we're not robots and bugs are possible :) Also we're
always happy to hear some suggestions, ideas, thoughts from you. Don't hesitate
to [create an issue](https://github.com/aistomin/maven-browser/issues/new). 
It will help us to make our project better. Thank you in advance!!!

## How to Contribute
Do you want to help us with the project? We will be more than just happy. 
Please: fork the repository, make changes, submit a pull request. We promise
to review your changes in the next couple of days and merge them to the master
branch, if they look correct. To avoid frustration, before sending us your pull
request please run full Maven build:

```
$ mvn clean install package
```
Keep in mind our [system requirements](#system-requirements).

                              
