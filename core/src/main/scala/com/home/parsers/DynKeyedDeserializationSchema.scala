package com.home.parsers

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.scala.createTypeInformation
import com.home.common.{Config, Constants}
import com.home.parsers.protobuf.{PBDeserialization, Schema}
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema
import java.util

/**
  * 将 protobuff 存储到map中
  */
class DynKeyedDeserializationSchema
  extends KeyedDeserializationSchema[Map[String, Any]] {

  val VALUE_CODE ="kafka.bili.value.code"
  val conf = Constants.appConf
  private val code =
    if (conf.hasPath(VALUE_CODE)) conf.getString(VALUE_CODE) else "ISO-8859-1"
  private val mapper = new ObjectMapper()
  override def deserialize(messageKey: Array[Byte],
                           message: Array[Byte],
                           topic: String, partition: Int,
                           offset: Long): Map[String, Any] = {
    Map("key" -> new String(messageKey, "UTF-8"),
      "topic" -> topic,
      "partition" -> partition,
      "offset" -> offset,
      "value" ->{
        val crawlData = Schema.CrawlData.parseFrom(message)
        val schema = crawlData.getSchema
        val contents = crawlData.getContentList
        schema
        // PBDeserialization.reader.readValue[Map[String, Any]](new String(message, code).getBytes(code)).mkString(";")
      }
    )
  }

  override def isEndOfStream(nextElement: Map[String, Any]): Boolean = false

  override def getProducedType: TypeInformation[Map[String, Any]] = createTypeInformation[Map[String, Any]]

}
