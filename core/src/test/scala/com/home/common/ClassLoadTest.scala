package com.home.common

import java.net.URI

object ClassLoadTest extends App {

  class ClassLoadTest

  //  val p = ClassLoadTest.getClass.getResource("/").getPath
  //  val ar = p.split("/")
  //  val sp = p.substring(1, p.indexOf("core"))
  //  println(p)
  //  println(sp)

  val path = RootClassPath.getRootClassPath()
  println(path)

}
