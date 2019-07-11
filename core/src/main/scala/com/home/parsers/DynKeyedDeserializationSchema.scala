package com.home.parsers

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.scala.createTypeInformation
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.{ObjectMapper, ObjectReader, SerializationFeature}
import com.fasterxml.jackson.dataformat.protobuf.ProtobufFactory
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufSchemaLoader
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.home.common.Constants
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema

/**
  * 将 protobuff 存储到map中
  */
class DynKeyedDeserializationSchema extends KeyedDeserializationSchema[Map[String, Any]] {

  lazy val mapper: ObjectReader = {
    val schema = ProtobufSchemaLoader.std.parse(Constants.schema)
    val mapper = new ObjectMapper(new ProtobufFactory) with ScalaObjectMapper
    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.PUBLIC_ONLY)
    mapper.registerModule(DefaultScalaModule).setSerializationInclusion(Include.NON_ABSENT)
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    mapper.readerFor(classOf[Map[String, Any]]).`with`(schema)
  }

  override def deserialize(messageKey: Array[Byte],
                           message: Array[Byte],
                           topic: String, partition: Int,
                           offset: Long): Map[String, Any] = {
    Map("key" -> new String(messageKey, "UTF-8"),
      "topic" -> topic,
      "partition" -> partition,
      "offset" -> offset,
      "value" -> new String(message)
    )
  }

  override def isEndOfStream(nextElement: Map[String, Any]): Boolean = false

  override def getProducedType: TypeInformation[Map[String, Any]] = createTypeInformation[Map[String, Any]]

}
