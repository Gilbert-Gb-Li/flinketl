
import com.home.common.LogFactory
import com.home.source.StreamingKafkaSource
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object MainStart {

  class MainStart
  val log = LogFactory.getLogger(classOf[MainStart])

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val ks = new StreamingKafkaSource(env)
    val ds = ks.kafkaSource("10.201.10.13:6667,10.201.10.14:6667,10.201.10.15:6667,10.201.10.33:6667,10.201.10.34:6667",
    "tv-danmaku-bili", "tv-danmaku-bili-01")
    import org.apache.flink.streaming.api.scala._
    log.info("kafka is ready ...")
    ds.map(e => {
      val o = e.getOrElse("offet", 0)
      val p = e.getOrElse("partition", -1)
      s"$p -> $o"
    }).writeAsText("file:///home/hadoop/bili/bigdata/p.txt")

    env.execute("BiliPrint")
  }


}

