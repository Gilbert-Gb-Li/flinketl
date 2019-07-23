package com.home.common

import java.io.File

import com.typesafe.config.ConfigFactory
import org.junit.Test

object ConfigTest {

  @Test
  def configTest1 (): Unit = {
    val conf = ConfigFactory.parseFile(new File("conf/test.conf"))
    println(conf.getIsNull("error"))
  }

  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.parseFile(new File("conf/test.conf"))
    println(conf.hasPathOrNull("trace")) // true
  }

}
