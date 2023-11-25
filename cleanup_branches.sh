#!/bin/bash
git fetch --prune; git branch -vv | egrep -v "(\[origin\/[a-zA-Z0-9/_-]+\])" | awk "{print \$1}" | xargs git branch -D

