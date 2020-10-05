#!/bin/bash
mvn clean install package javadoc:javadoc
bash <(curl -s https://codecov.io/bash)