
import com.home.common.LogFactory
import com.home.source.{StreamKafkaSource}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object MainStart {

  class MainStart
  val logger = LogFactory.getLogger(classOf[MainStart])

  def main(args: Array[String]): Unit = {
    logger.info("flink is ready ...")
    System.setProperty("HADOOP_USER_NAME", "hadoop")
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val ks = new StreamKafkaSource(env)
    val ds = ks.kafkaSource(
      "10.201.10.13:6667,10.201.10.14:6667,10.201.10.15:6667,10.201.10.33:6667,10.201.10.34:6667",
    "tv-danmaku-bili", "tv-danmaku-bili-01")
    logger.info("kafka is ready ...")
    ks.streamFromKafka(ds)
      .filter(_.contains("partition: 0"))
      .writeAsText("file:///home/developer/bili/bigdata-build-1.0-SNAPSHOT/p.txt")
      //.print()
    env.execute("BiliPrint")

  }


}

