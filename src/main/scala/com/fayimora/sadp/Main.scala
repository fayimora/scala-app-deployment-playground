package com.fayimora.sadp

import zio.*
import zhttp.http.*
import zhttp.service.Server

object Main extends ZIOAppDefault:
  val app = Http.collect[Request] {
    case Method.GET -> !! / "hello" =>
      Response.text("Hello World!")
    case Method.GET -> !! / "hi" =>
      Response.text("Hi World!")
  }

  override def run =
    Server.start(8090, app).exitCode
