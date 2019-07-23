
import com.home.common.LogFactory
import com.home.source.{StreamKafkaSource, StreamingKafkaSource}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object MainStart {

  class MainStart
  val log = LogFactory.getLogger(classOf[MainStart])

  def main(args: Array[String]): Unit = {
    log.info("flink is ready ...")
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val ks = new StreamKafkaSource(env)
    val ds = ks.kafkaSource(
      "10.201.10.13:6667,10.201.10.14:6667,10.201.10.15:6667,10.201.10.33:6667,10.201.10.34:6667",
    "tv-danmaku-bili", "tv-danmaku-bili-01")
    log.info("kafka is ready ...")
    ks.streamFromKafka(ds)
      .filter(_.contains("partition: 0"))
      .writeAsText("file:///home/hadoop/bili/bigdata/bigdata-build-1.0-SNAPSHOT/p.txt")
      //.writeAsText("file:///d:/haima/bili")

    env.execute("BiliPrint")
  }


}

