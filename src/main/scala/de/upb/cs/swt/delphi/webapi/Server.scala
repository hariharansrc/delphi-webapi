package de.upb.cs.swt.delphi.webapi

import akka.http.scaladsl.server.HttpApp

/**
  * Web server configuration for Delphi web API.
  */
object Server extends HttpApp {

   override def routes =
      path("version") { version } ~
        path("features") { features } ~
        pathPrefix("search" / Remaining) { query => search(query) } ~
        pathPrefix("retrieve" / Remaining) { identifier => retrieve(identifier) }


  private def version = {
    get {
      complete {
        BuildInfo.version
      }
    }
  }

  private def features = {
    get {
      complete {
        "features"
      }
    }
  }

  def retrieve(identifier: String) = {
    get {
      complete(identifier)
    }
  }

  def search(query: String) = {
    get {
      complete {
        query
      }
    }
  }

  def main(args: Array[String]): Unit = {
    Server.startServer("0.0.0.0", 8080)
  }


}


