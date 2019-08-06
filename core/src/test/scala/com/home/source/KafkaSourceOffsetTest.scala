package com.home.source

import java.util.Properties
import java.{lang, util}

import com.home.common.LogFactory
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010
import com.home.parsers.DynKeyedDeserializationSchema
import org.apache.flink.api.common.functions.{MapFunction, RichMapFunction}
import org.apache.flink.api.common.state._
import org.apache.flink.configuration.Configuration
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction
import org.apache.flink.streaming.connectors.kafka.internals.KafkaTopicPartition

/**
  * KAFKA版本：0.10.1
  * 验证自定义offset --> 失败
  */
class KafkaSourceOffsetTest (env: StreamExecutionEnvironment) extends CheckpointedFunction {

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
  private val logger = LogFactory.getLogger(classOf[KafkaSourceOffsetTest])

  def kafkaSource (servers: String, topic: String, groupId: String): DataStream[Map[String, Any]] = {
    val prop = new Properties()
    prop.setProperty(SERVERS,servers)
    prop.setProperty(GROUPID,groupId)
    logger.info("kafka.broker [{}], topic [{}], group.id [{}]", servers, topic, groupId)
    val consumer010 = new FlinkKafkaConsumer010[Map[String, Any]](topic, new DynKeyedDeserializationSchema(), prop)

    consumer010.setCommitOffsetsOnCheckpoints(true)
    PartitonAndOffset match {
      case Some(x) => consumer010.setStartFromSpecificOffsets(x)
      case None =>
    }
    env.addSource(consumer010)

  }

  def streamFromKafka (ds: DataStream[Map[String, Any]]): DataStream[String] = {

    ds.map(new OffsetMapFunction)

  }


  override def snapshotState(context: FunctionSnapshotContext): Unit = {}

  /** 此处未执行初始化，原因不详 */
  override def initializeState(context: FunctionInitializationContext): Unit = {
    val descriptor = new ListStateDescriptor[util.HashMap[Int, java.lang.Long]](
      "offset", classOf[util.HashMap[Int, java.lang.Long]])
    logger.info(
      """
        | >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        | kafka initializeState ok!
        | >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
      """.stripMargin)
    if (context.isRestored) {
      val offset = context.getOperatorStateStore.getListState(descriptor)
      val map = offset.get().iterator().next()
      setKafkaOffset(map)
      logger.info(
        """
          | >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
          | partition 0 's offset: {}
          | >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        """.stripMargin, map.get(0))
    }
  }

  private def setKafkaOffset (offset: util.HashMap[Int, java.lang.Long]): Option[util.HashMap[KafkaTopicPartition, java.lang.Long]] = {
    offset match {
      case null => None
      case _ =>
        val po: util.HashMap[KafkaTopicPartition, java.lang.Long] = new util.HashMap()
        import scala.collection.JavaConversions._
        for (o <- offset.entrySet) {
          po.put(new KafkaTopicPartition("tv-danmaku-bili", o.getKey), o.getValue)
        }
        Some(po)
    }
  }

}


// ------------------------------------------------------------------------------------ //

/** MapFunction + CheckpointedFunction
  * 抽取每条数据的partition及offset写入ListState中
  *
  * 1. 继承MapFunction可以配合CheckpointedFunction使用OperatorState
  * 2. 变量封装在MapFunction中可解决闭包问题，使用状态来解决闭包问题
  */
class OffsetMapFunction
  extends MapFunction[Map[String, Any], String]
    with CheckpointedFunction {

  @transient
  private var offsetState: ListState[util.HashMap[Int, java.lang.Long]] = _
  private var offset: util.HashMap[Int, java.lang.Long] = _


  override def map(value: Map[String, Any]): String = {
    val p = value.getOrElse("partition", -1).toString.toInt
    val o = value.getOrElse("offset", 0).toString.toLong
    offset.put(p, o)
    s"partition: $p -> offset: $o"

  }

  /**
    * snapshot时更新状态
    * 对于较复杂的状态数据结构比较使用
    */
  override def snapshotState(context: FunctionSnapshotContext): Unit = {
    offsetState.clear()
    offsetState.add(offset)
  }

  /**
    * 状态初始化
    * 既可以初始化KeyedState也可以初始化OperatorState
    */
  override def initializeState(context: FunctionInitializationContext): Unit = {
    offset = new util.HashMap[Int, java.lang.Long](10)
    offsetState = context.getOperatorStateStore.getListState[util.HashMap[Int, java.lang.Long]] (
      new ListStateDescriptor("offset", classOf[util.HashMap[Int, java.lang.Long]])
    )
  }
}


// ------------------------------------------------------------------------------------ //

/** RichMapFunction
  * 抽取每条数据的partition及offset写入ListState中
  *
  * 1. 继承RichMapFunction时必须使用KeyedState
  * 2. 变量封装在MapFunction中可解决闭包问题，使用状态来解决闭包问题
  */
class OffsetRichMapFunction
  extends RichMapFunction[Map[String, Any], String] {

  @transient
  private var offsetState: ListState[util.HashMap[Int, java.lang.Long]] = _
  private var offset: util.HashMap[Int, java.lang.Long] = _

  /** 1.open方法在map调用前执行，
    *   可用来完成一些诸如数据库初始化工作
    * 2.也可以用来初始化状态，效果与initializeState相同
    * 3.用状态时必须使用KeyedState，否则会出现各种不相关错误
    */
  override def open(parameters: Configuration): Unit = {
    super.open(parameters)
    offset = new util.HashMap[Int, java.lang.Long](10)
    offsetState = getRuntimeContext.getListState[util.HashMap[Int, java.lang.Long]] (
      new ListStateDescriptor("offset", classOf[util.HashMap[Int, java.lang.Long]])
    )
  }

  /**
    * 此处更新的状态也会被checkpoint
    */
  override def map(value: Map[String, Any]): String = {
    val p = value.getOrElse("partition", -1).toString.toInt
    val o = value.getOrElse("offset", 0).toString.toLong
    offset.put(p, o)
    s"partition: $p -> offset: $o"

  }

  override def close(): Unit = super.close()

}


