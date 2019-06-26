package com.home.common

object RootClassPath {

  def getRootClassPath(): String = {
    /** 当前jar包所在的目录 */
    val target = RootClassPath.getClass.getResource("/").getPath
    val i = target.indexOf("lib")
    if (i > 0) target.substring(0, i) else ""
  }

}

class RootClassPath