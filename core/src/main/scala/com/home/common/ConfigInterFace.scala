package com.home.common

trait ConfigInterFace {

  def getString (key: String): String

  def hasPath (key: String): Boolean

}
