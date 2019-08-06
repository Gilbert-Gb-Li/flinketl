package com.home.source

import java.util.Properties

import com.home.common.LogFactory
import com.home.parsers.DynKeyedDeserializationSchema
import org.apache.flink.runtime.state.filesystem.FsStateBackend
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup

/**
  * KAFKA版本：0.10.1
  */
class StreamKafkaSource (env: StreamExecutionEnvironment) {

  private val CHECKPOINT_INTERVAL = 30000
  private val CHECKPOINT_MIN_PAUSE = 1000
  private val CHECKPOINT_TIMEOUT = 120000
  private val CHECKPOINT_MAX_CONCURRENT = 1
  private val SERVERS = "bootstrap.servers"
  private val GROUPID = "group.id"


  env.enableCheckpointing(CHECKPOINT_INTERVAL)
  env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
  env.getCheckpointConfig.setMinPauseBetweenCheckpoints(CHECKPOINT_MIN_PAUSE)
  env.getCheckpointConfig.setCheckpointTimeout(CHECKPOINT_TIMEOUT)
  env.getCheckpointConfig.setMaxConcurrentCheckpoints(CHECKPOINT_MAX_CONCURRENT)
  env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
  // env.setStateBackend(new FsStateBackend("hdfs://hadoop100:9000/flink/checkpoints",true))
  env.setStateBackend(new FsStateBackend("hdfs://MVP-HADOOP90:8020/data/bili/origin/live_info/2019-07-02/00"))

  private val logger = LogFactory.getLogger(classOf[StreamKafkaSource])

  def kafkaSource (servers: String, topic: String, groupId: String): DataStream[Map[String, Any]] = {
    val prop = new Properties()
    prop.setProperty(SERVERS, servers)
    prop.setProperty(GROUPID, groupId)
    logger.info("kafka.broker [{}], topic [{}], group.id [{}]", servers, topic, groupId)
    val consumer010 = new FlinkKafkaConsumer010[Map[String, Any]](topic, new DynKeyedDeserializationSchema(), prop)
    import org.apache.flink.api.scala._
    consumer010.setCommitOffsetsOnCheckpoints(true)
    env.addSource(consumer010)

  }

  def streamFromKafka (ds: DataStream[Map[String, Any]]): DataStream[String] = {
    import org.apache.flink.api.scala._
    ds.map(e=> {
      if (e.getOrElse("partition", -1) == 0) {
        val o = e.getOrElse("offset", 0).toString
        s"partition: 0 -> offset: '$o'"
      } else
        "-1"
    })
  }


}

