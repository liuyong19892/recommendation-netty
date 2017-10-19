package com.xmtj.netty.log

import org.slf4j.LoggerFactory

/**
  * Created by jay on 2016/3/4.
  */

trait Logger {
  val logger  = LoggerFactory.getLogger(this.getClass);
}

object LoggerUtil extends Logger
object ErrorLogger extends Logger
