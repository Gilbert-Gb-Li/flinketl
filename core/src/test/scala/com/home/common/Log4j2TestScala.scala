package com.home.common


object Log4j2TestScala {

  class Log4j2TestScala

  def main(args: Array[String]): Unit = {
    val logger = LogFactory.getLogger(classOf[Log4j2TestScala])
    logger.debug("common: this is a debug message!")
    logger.info("common: this is a info message!")

  }

}
