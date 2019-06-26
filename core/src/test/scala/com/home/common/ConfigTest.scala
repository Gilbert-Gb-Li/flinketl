package com.home.common

import java.io.File

import com.typesafe.config.ConfigFactory

object ConfigTest {


  def main(args: Array[String]): Unit = {

    val conf = ConfigFactory.parseFile(new File("conf/test.conf"))

    println(conf.getString("error"))
  }

}
