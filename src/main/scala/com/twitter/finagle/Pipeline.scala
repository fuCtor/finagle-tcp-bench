package com.twitter.finagle

import java.nio.charset.StandardCharsets

import io.netty.channel.ChannelPipeline
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.{StringDecoder, StringEncoder}

object Pipeline extends (ChannelPipeline => Unit) {
  def apply(pipeline: ChannelPipeline): Unit = {
    pipeline.addLast("frameDecoder", new LineBasedFrameDecoder(1 << 16))
    pipeline.addLast("messageDecode", new StringDecoder(StandardCharsets.UTF_8))
    pipeline.addLast("messageEncode", new StringEncoder(StandardCharsets.UTF_8))
  }
}
