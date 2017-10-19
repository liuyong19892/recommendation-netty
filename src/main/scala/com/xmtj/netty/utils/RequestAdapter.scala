package com.xmtj.netty.utils

import com.xmtj.netty.log.Logger
import io.netty.buffer.ByteBuf
import io.netty.util.CharsetUtil

/**
  * Created by Jay on 2016/2/15.
  */
object RequestAdapter extends Logger {

  def mklog(httpEntity: ByteBuf): String = {
    httpEntity.toString(CharsetUtil.UTF_8)
  }

  private def byteBuf2ByteArray(httpEntity: ByteBuf) : Array[Byte] = {
    val bytes= new Array[Byte](httpEntity.readableBytes())
    httpEntity.readBytes(bytes)
    bytes
  }

}
