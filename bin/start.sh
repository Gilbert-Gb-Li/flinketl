#!/bin/bash

# 获取JAR根目录
APP_BASE="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

# FLINK 位置
FLINK_HOME="/home/hadoop/bili/flink-1.6.4"

# CLASSPATH Jar及依赖的位置
# 多次调用 -C 参数添加依赖JAR包，中间以空格隔开
CLASSPATH=''
for name in ${APP_BASE}/lib/*.jar
do
    name="file://$name"
    CLASSPATH="${CLASSPATH} -C ${name}"
done

# java -cp $CLASSPATH MainStart

# 执行命令
# 末尾添加入口类所在的JAR包
${FLINK_HOME}/bin/flink run ${CLASSPATH} -c MainStart ${APP_BASE}/lib/bigdata-start-1.0-SNAPSHOT.jar
