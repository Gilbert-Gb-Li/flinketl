#!/bin/bash

# 获取根目录
APP_BASE="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

CLASSPATH=${APP_BASE}/lib
for name in ${APP_BASE}/lib/*.jar
do
    CLASSPATH=${CLASSPATH}:${name}
done

java -cp $CLASSPATH MainStart