package com.home.common


object Log4j2TestScala {

  class Log4j2TestScala

  def main(args: Array[String]): Unit = {
    val logger = LogFactory.getLogger(classOf[Log4j2TestScala])
    logger.debug("common: this is a debug message!")
    logger.info("common: this is a info message!")

    /** 使用log4j2 配置文件中的Logger name 创建logger对象来指定输出*/
    val logger1 = LogFactory.getLogger("MainLogger")
    logger1.error("common: this is a error message!")


  }

}
