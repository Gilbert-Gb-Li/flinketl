import java.io.File

import com.home.common.{Constants, LogFactory}
import com.typesafe.config.ConfigFactory

object MainStart {

  class MainStart

  def main(args: Array[String]): Unit = {

    val log = LogFactory.getLogger("MainLogger")
    val conf = ConfigFactory.parseFile(new File(Constants.APP_TEST_PATH))
    log.debug("this a {} message", conf.getString("debug"))
    log.info("this a {} message", conf.getString("info"))
    log.error("this a {} message", conf.getString("error"))


  }


}

