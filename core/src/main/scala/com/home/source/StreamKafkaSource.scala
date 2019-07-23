package com.home.source

import java.util
import java.util.Properties

import com.home.common.LogFactory
import com.home.parsers.DynKeyedDeserializationSchema
import org.apache.flink.api.common.state._
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.api.environment.CheckpointConfig
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010
import org.apache.flink.streaming.connectors.kafka.internals.KafkaTopicPartition


/**
  * KAFKA版本：0.10.1
  */
class StreamKafkaSource (env: StreamExecutionEnvironment) extends CheckpointedFunction {

  private val CHECKPOINT_INTERVAL = 30000
  private val CHECKPOINT_MIN_PAUSE = 1000
  private val CHECKPOINT_TIMEOUT = 120000
  private val CHECKPOINT_MAX_CONCURRENT = 1
  private val SERVERS = "bootstrap.servers"
  private val GROUPID = "group.id"

  import org.apache.flink.api.scala._
  env.enableCheckpointing(CHECKPOINT_INTERVAL)
  env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
  env.getCheckpointConfig.setMinPauseBetweenCheckpoints(CHECKPOINT_MIN_PAUSE)
  env.getCheckpointConfig.setCheckpointTimeout(CHECKPOINT_TIMEOUT)
  env.getCheckpointConfig.setMaxConcurrentCheckpoints(CHECKPOINT_MAX_CONCURRENT)
  env.getCheckpointConfig.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
  //设置statebackend
  //env.setStateBackend(new RocksDBStateBackend("hdfs://hadoop100:9000/flink/checkpoints",true));

  private val logger = LogFactory.getLogger(classOf[StreamingKafkaSource])
  @transient
  private var offsetState: ListState[Map[Int, java.lang.Long]] = _

  private var offset: Map[Int, java.lang.Long] = _
  def kafkaSource (servers: String, topic: String, groupId: String): DataStream[Map[String, Any]] = {
    val prop = new Properties()
    prop.setProperty(SERVERS,servers)
    prop.setProperty(GROUPID,groupId)
    logger.info("kafka.broker [{}], topic [{}], group.id [{}]", servers, topic, groupId)
    val consumer010 = new FlinkKafkaConsumer010[Map[String, Any]](topic, new DynKeyedDeserializationSchema(), prop)

    /** 是否自动提交offset
      * true : 自动提交，程序重启后会根据上次读取的位置续读，默认值
      *        但可能出现flink checkpoint与自动提交的offset不一致情况
      * false: 不自动提交，须使用flink状态手动存储offset并编写kafka续读逻辑
      */
    consumer010.setCommitOffsetsOnCheckpoints(false)
    fromSpecificOffsets(consumer010)
    env.addSource(consumer010)

  }

  def streamFromKafka (ds: DataStream[Map[String, Any]]): DataStream[String] = {

    ds.map(e=> {
      val p = e.getOrElse("partition", -1).toString.toInt
      val o = e.getOrElse("offset", 0).toString.toLong
      Map(p -> o)
      s"partition: $p -> offset: $o"
    })

  }


  override def snapshotState(context: FunctionSnapshotContext): Unit = {
    val oldMap = offsetState.get().iterator().next()
    offset ++= oldMap
    offsetState.clear()
    offsetState.add(offset)

  }

  override def initializeState(context: FunctionInitializationContext): Unit = {
    val descriptor = new ListStateDescriptor("offset", classOf[Map[Int, java.lang.Long]])
    logger.info(
      """
        | >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        | kafka initializeState ok!
        | >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
      """.stripMargin)
    if (context.isRestored) {
      val list = context.getOperatorStateStore.getListState(descriptor)
      offset = list.get().iterator().next()
      logger.info(
        """
          | >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
          | partition's offset: {}
          | >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        """.stripMargin, offset.mkString(";"))
    }
  }

  private def fromSpecificOffsets (consumer: FlinkKafkaConsumer010[Map[String, Any]]): Unit = {
    if (offset != null && offset.nonEmpty) {
      val po: util.HashMap[KafkaTopicPartition, java.lang.Long] = new util.HashMap()
      for (e <- offset) {
        po.put(new KafkaTopicPartition("tv-danmaku-bili", e._1), e._2)
      }
      consumer.setStartFromSpecificOffsets(po)
    }
  }

}

