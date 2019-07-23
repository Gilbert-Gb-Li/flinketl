package com.home.common

import java.io.File
import com.typesafe.config.ConfigFactory

case class Config(private val path: String) extends ConfigInterFace {

  private val conf = ConfigFactory.parseFile(new File(path))

  override def getString(key: String): String = conf.getString(key)

  override def hasPath(key: String): Boolean = conf.hasPath(key)

}