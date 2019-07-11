package com.home.source

import java.util.Properties

import com.home.common.LogFactory
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010
import com.home.parsers.DynKeyedDeserializationSchema

/**
  * KAFKA版本：0.10.1
  */
class StreamingKafkaSource (env: StreamExecutionEnvironment) {

  private val CHECKPOINT_INTERVAL = 30000
  private val CHECKPOINT_MIN_PAUSE = 1000
  private val CHECKPOINT_TIMEOUT = 120000
  private val CHECKPOINT_MAX_CONCURRENT = 1
  private val SERVERS = "bootstrap.servers"
  private val GROUPID = "group.id"

  val logger = LogFactory.getLogger(classOf[StreamingKafkaSource])

  def kafkaSource (servers: String, topic: String, groupId: String): DataStream[Map[String, Any]] = {

    import org.apache.flink.api.scala._
    env.enableCheckpointing(CHECKPOINT_INTERVAL)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(CHECKPOINT_MIN_PAUSE)
    env.getCheckpointConfig.setCheckpointTimeout(CHECKPOINT_TIMEOUT)
    env.getCheckpointConfig.setMaxConcurrentCheckpoints(CHECKPOINT_MAX_CONCURRENT)
    env.getCheckpointConfig.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)

    //设置statebackend
    //env.setStateBackend(new RocksDBStateBackend("hdfs://hadoop100:9000/flink/checkpoints",true));

    val prop = new Properties()
    prop.setProperty(SERVERS,servers)
    prop.setProperty(GROUPID,groupId)
    logger.info("kafka.broker [{}], topic [{}], group.id [{}]", servers, topic, groupId)
    val consumer010 = new FlinkKafkaConsumer010[Map[String, Any]](topic, new DynKeyedDeserializationSchema(), prop)
    consumer010.setCommitOffsetsOnCheckpoints(false)
    env.addSource(consumer010)
  }

}
