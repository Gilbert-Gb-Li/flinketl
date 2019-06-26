package com.home.common

object Constants {

  /** 根目录，也可通过 -D 传入参数的方式获取 */
  val ROOT = RootClassPath.getRootClassPath()
  /** 相对于根目录父module */
  val APP_CONF_PATH = s"${ROOT}conf/app.conf"
  val APP_TEST_PATH = s"${ROOT}conf/test.conf"

}
