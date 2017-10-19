package com.xmtj.netty.handler

import java.net.{URI, URLDecoder}

import com.xmtj.netty.log.{ErrorLogger, Logger, LoggerUtils}
import hbaseutill.HbaseClient
import http.RequestParamsParser
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.HttpVersion._
import io.netty.handler.codec.http._
import json.JsonUtils
import model.Result

class ServerHandler extends SimpleChannelInboundHandler[FullHttpRequest] with Logger{

  val OK = new HttpResponseStatus(200, "OK")
  override def channelRead0(ctx: ChannelHandlerContext, msg: FullHttpRequest): Unit = {
    val start = System.currentTimeMillis()
    import scala.collection.JavaConverters._
    val res = new Result(100, "No right param found! e.g: ?user_id=421231231&num=5 or ?comic_id=1232132&num=5", List.empty[Integer].asJava)
    val uri = URI.create(msg.uri())

    try{
      val map = RequestParamsParser.getRequestParams(ctx, msg)
      if(msg.uri().contains("favicon.ico")) return
      val udid = map.getOrDefault("user_id","")
      val comicId = map.getOrDefault("comic_id","")
      val num = map.getOrDefault("num","20").toInt


      uri.getPath match {
        case "/recommend/user" => {
          if(udid.nonEmpty) {
            setResult(res, udid, "mk_recommandations", num)
          }
        }
        case "/recommend/comic" => {
          if(comicId.nonEmpty) {
            setResult(res, comicId, "mk_items", num)
          }
        }
      }


      if(res.getData.isEmpty) {
        res.setCode(201)
        res.setMsg("No recommendation data found!")
      }
      response(ctx, JsonUtils.toJsonString(res))
    } catch {
      case e:Exception =>
        ErrorLogger.logger.info(s"app error: ${e.getMessage}. " + LoggerUtils.getTrace(e))
        res.setMsg(s"app error: ${e.getMessage}. ")
        response(ctx, JsonUtils.toJsonString(res))
    } finally {
      val end = System.currentTimeMillis()
      logger.info(s"client spent time : ${(end - start)} ms.")
    }
  }

  private def setResult(res: Result, comicId: String, table: String, num:Int): Unit = {
    import scala.collection.JavaConverters._
    val its = HbaseClient.getData(comicId, table)
    if (its.nonEmpty) {
      res.setData(its.split(",").map(x => new Integer(x.toInt)).take(num).toList.asJava)
      res.setCode(200)
      res.setMsg("Success")
    }
  }

  private def response(ctx: ChannelHandlerContext, msg: String) = {
    val response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(msg.getBytes("UTF-8")))
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=utf-8")
    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes().toString)
    response.content().readableBytes()
    ctx.writeAndFlush(response)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    ErrorLogger.logger.info(s"netty error: ${cause.getMessage}. " + LoggerUtils.getTrace(cause))
    ctx.close
  }



  private def byteBuf2ByteArray(httpEntity: ByteBuf) : Array[Byte] = {
    val bytes= new Array[Byte](httpEntity.readableBytes())
    httpEntity.readBytes(bytes)
    bytes
  }

}

object ServerHandler{
  def main(args: Array[String]): Unit = {
    println(URLDecoder.decode("\"comic_title\":\"%E6%96%B0%E7%89%88%E7%88%86%E7%AC%91%E6%A0%A1%E5%9B%AD\"","utf-8"))
  }
}