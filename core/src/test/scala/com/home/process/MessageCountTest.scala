package com.home.process

import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.runtime.state.{FunctionInitializationContext, FunctionSnapshotContext}
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction

/**
  * 状态测试 -- 失败
  * 自定义状态值无法保存或读取
  */

class MessageCountTest extends RichMapFunction[(Int, Map[String, Any]), String]
with CheckpointedFunction {

  private var countState: ValueState[Int] = _
  private var count = 0

  override def map(value: (Int, Map[String, Any])): String = {
    count += 1
    if (value._1 == 0) {
      s"partition: 0 -> count: '$count'"
    } else
      "-1"
  }

  override def snapshotState(context: FunctionSnapshotContext): Unit = {
      countState.update(count)
  }

  override def initializeState(context: FunctionInitializationContext): Unit = {
    countState = getRuntimeContext.getState[Int](
      new ValueStateDescriptor[Int]("count", classOf[Int])
    )
    if (context.isRestored) {
      count = countState.value()
    }

  }

}

