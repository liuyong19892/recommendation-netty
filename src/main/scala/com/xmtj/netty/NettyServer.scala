package com.xmtj.netty

import com.xmtj.netty.handler.ChildChannelHandler
import com.xmtj.netty.utils.Constants
import hbaseutill.HbaseClient
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel

/**
  * Created by Jay on 16/5/23.
  */
class NettyServer(port: Int) {
  def run = {

    val bossGroupSize = Runtime.getRuntime.availableProcessors * Constants.netty_group
    val workerGroupSize = Runtime.getRuntime.availableProcessors * Constants.netty_thread
    val bossGroup = new NioEventLoopGroup(bossGroupSize)
    val workerGroup = new NioEventLoopGroup(workerGroupSize)
    try {
      val b = new ServerBootstrap()
      b.group(bossGroup, workerGroup)
        .channel(classOf[NioServerSocketChannel])
        .childHandler(new ChildChannelHandler)
        .option(ChannelOption.SO_BACKLOG, int2Integer(Constants.back_log))
//        .childOption(ChannelOption.SO_SNDBUF, int2Integer(65535))
        .childOption(ChannelOption.SO_RCVBUF, int2Integer(65535))
        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
        .childOption(ChannelOption.SO_KEEPALIVE, boolean2Boolean(true))

      val f = b.bind(port).sync()
      if(f.isSuccess) println(s"Netty server started up on port: ${port}")

      f.channel().closeFuture().sync()
    } catch {
      case e:Exception => e.printStackTrace()
    }finally {
      HbaseClient.close()
      workerGroup.shutdownGracefully()
      bossGroup.shutdownGracefully()
    }

  }
}

object NettyServer{


  def main(args: Array[String]) {
    HbaseClient.init()
    new NettyServer(Constants.engine_port).run
  }
}