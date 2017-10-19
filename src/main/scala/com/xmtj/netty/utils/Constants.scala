package com.xmtj.netty.utils

import java.io.{FileReader, IOException}
import java.util.Properties

import com.typesafe.config.ConfigFactory

/**
  * Created by sky-wind on 2016/2/15.
  */
object Constants {

  val config = ConfigFactory.load()
//  val config = ConfigFactory.load("application_dev")

  def getOrElse[T](name: String, default: T) = {
    import scala.collection.JavaConversions._
    if (config.hasPath(name)){
      default match {
        case default: String        ⇒ config.getString(name)
        case default: Int           ⇒ config.getInt(name)
        case default: Long          ⇒ config.getLong(name)
        case default: Boolean       ⇒ config.getBoolean(name)
        case default: Double        ⇒ config.getDouble(name)
        case default: List[String]  ⇒ config.getStringList(name).toList
        case default: List[Int]     ⇒ config.getIntList(name).toList
        case default: List[Long]    ⇒ config.getLongList(name).toList
        case default: List[Boolean] ⇒ config.getBooleanList(name).toList
        case default: List[Double]  ⇒ config.getDoubleList(name).toList
        case _                      ⇒ config.getAnyRef(name)
      }
    } else default
  }

  val netty_group = config.getInt("netty.group.size")
  val netty_thread = config.getInt("netty.thread.size")
  val back_log = config.getInt("netty.backlog")

  val engine_port = config.getInt("engine.port")

  def loadProperties(filename: String): Properties = {
   // logger.info(s"load properties filename: $filename")
    val props: Properties = new Properties
    var reader: FileReader = null
    try {
      reader = new FileReader(filename)
      props.load(reader)
    } catch {
      case e: IOException ⇒
       // logger.error(s"load properties error! filename: $filename", e)
    } finally {
      if (null != reader) reader.close()
    }
    //logger.info(s"load properties filename: $filename, $props")
    props
  }
  val withDefault: (Option[String]) ⇒ String = {
    case (Some(s)) ⇒ s.trim
    case _         ⇒ ""
  }

}
