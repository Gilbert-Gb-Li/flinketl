package com.home.common

import java.io.File
import com.typesafe.config.ConfigFactory

object Config extends ConfigInterFace {

  override def getString(path: String, key: String): String = {
    ConfigFactory.parseFile(new File(path))
      .getString(key)
  }

}