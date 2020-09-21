#!/bin/bash
mvn clean install package
bash <(curl -s https://codecov.io/bash)