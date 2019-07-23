package com.home.source

import java.util.Properties
import java.util

import com.home.common.LogFactory
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010
import com.home.parsers.DynKeyedDeserializationSchema
import org.apache.flink.api.common.functions.{MapFunction, RichMapFunction}
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.connectors.kafka.internals.KafkaTopicPartition

/**
  * KAFKA版本：0.10.1
  */
class StreamingKafkaSource (env: StreamExecutionEnvironment) extends CheckpointedFunction {

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

  private var PartitonAndOffset: Option[util.Map[KafkaTopicPartition, java.lang.Long]] = None
  private val logger = LogFactory.getLogger(classOf[StreamingKafkaSource])

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
    PartitonAndOffset match {
      case Some(x) => consumer010.setStartFromSpecificOffsets(x)
      case None =>
    }
    env.addSource(consumer010)

  }

  def streamFromKafka (ds: DataStream[Map[String, Any]]): DataStream[String] = {

    ds.map(e=> {
      val k = e.getOrElse("partition", -1).toString.toInt
      (k, e)
    }).keyBy(0).map(new OffsetMapFunction)

  }


  override def snapshotState(context: FunctionSnapshotContext): Unit = {}

  override def initializeState(context: FunctionInitializationContext): Unit = {
    val descriptor = new MapStateDescriptor("offset", classOf[Int], classOf[java.lang.Long])
    logger.info(
      """
        | >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        | kafka initializeState ok!
        | >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
      """.stripMargin)
    if (context.isRestored) {
      val offset = context.getKeyedStateStore.getMapState(descriptor)
      setKafkaOffset(offset)
      logger.info(
        """
          | >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
          | partition 0 's offset: {}
          | >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        """.stripMargin, offset.get(0))
    }
  }

  private def setKafkaOffset (offset: MapState[Int, java.lang.Long]): Option[util.HashMap[KafkaTopicPartition, java.lang.Long]] = {
    offset match {
      case null => None
      case _ =>
        val po: util.HashMap[KafkaTopicPartition, java.lang.Long] = new util.HashMap()
        val offsets = offset.iterator()
        while (offsets.hasNext) {
          val o = offsets.next()
          po.put(new KafkaTopicPartition("tv-danmaku-bili", o.getKey), o.getValue)
        }
        Some(po)
    }
  }

}


/**
  * 1. 抽取partition及offset写入state中
  * 2. 将partition为0的offset输出
  */
class OffsetMapFunction
  extends RichMapFunction[(Int,Map[String, Any]), String]
  with CheckpointedFunction {

  @transient
  private var offsetState: MapState[Int, java.lang.Long] = _
  private var t: (Int, Long) = _


  /** 1.open方法在map调用前执行，
    *   可用来完成一些诸如数据库初始化工作
    * 2.也可以用来初始化状态，效果与initializeState相同
    * 3.用状态时必须使用KeyedState，否则会出现各种不相关错误
    */
  override def open(parameters: Configuration): Unit = {
    super.open(parameters)
    offsetState = getRuntimeContext.getMapState[Int, java.lang.Long] (
      new MapStateDescriptor("offset", classOf[Int], classOf[java.lang.Long])
    )
  }

  override def map(value: (Int, Map[String, Any])): String = {
    val p = value._1
    val o = value._2.getOrElse("offset", 0).toString.toLong
    t = (p, o)
    if (p == 0) {s"partition: $p -> offset: $o"}
    else "-1"

  }

  override def close(): Unit = {super.close()}

  override def snapshotState(context: FunctionSnapshotContext): Unit = {
    offsetState.put(t._1, t._2)
  }

  override def initializeState(context: FunctionInitializationContext): Unit = {}
}


