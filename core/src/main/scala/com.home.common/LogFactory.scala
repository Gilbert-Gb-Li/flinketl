package com.home.common

import java.io.{File, FileInputStream}
import com.typesafe.config.ConfigFactory
import org.apache.logging.log4j.core.config.{ConfigurationSource, Configurator}
import org.slf4j.{Logger, LoggerFactory}

object LogFactory {

  def getLog4j2: Unit = {
    var source: ConfigurationSource = null
    val log4jFile = new File(getLog4j2Path)
    try
        if (log4jFile.exists) {
          source = new ConfigurationSource(new FileInputStream(log4jFile), log4jFile)
          Configurator.initialize(null, source)
        }
        else {
          System.out.println("log init failed !")
          System.exit(1)
        }
    catch {
      case e: Exception =>
        e.printStackTrace()
        System.exit(2)
    }
  }

  def getLog4j2Path: String = {
    val conf = ConfigFactory.parseFile(new File("conf/app.conf"))
    conf.getString("log4j2.path")
  }

  def getLogger(c: Class[_]): Logger = {
    LoggerFactory.getLogger(c)
  }

  def getLogger(s: String): Logger = {
    LoggerFactory.getLogger(s)
  }

  getLog4j2
}
