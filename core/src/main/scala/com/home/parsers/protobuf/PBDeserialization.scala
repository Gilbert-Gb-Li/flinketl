package com.home.parsers.protobuf

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.{ObjectMapper, ObjectReader, SerializationFeature}
import com.fasterxml.jackson.dataformat.protobuf.ProtobufFactory
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufSchemaLoader
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.home.common.Constants

object PBDeserialization {

  private val schema = ProtobufSchemaLoader.std.parse(Constants.schema)
  private val mapper = new ObjectMapper(new ProtobufFactory) with ScalaObjectMapper
  mapper.setVisibility(PropertyAccessor.FIELD, Visibility.PUBLIC_ONLY)
  mapper.registerModule(DefaultScalaModule).setSerializationInclusion(Include.NON_ABSENT)
  mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
  val reader = mapper.readerFor(classOf[Map[String, Any]]).`with`(schema)

}
