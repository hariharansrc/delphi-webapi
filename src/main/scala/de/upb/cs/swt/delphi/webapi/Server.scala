package de.upb.cs.swt.delphi.webapi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.StdIn

/**
  * Web server configuration for Delphi web API.
  */
object Server {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val route =
      path("version") { version } ~
      pathPrefix("search" / Remaining) { query => search(query) } ~
      path("features") { features } ~
      pathPrefix("retrieve" / Remaining) { identifier => retrieve(identifier) }


    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  private def version = {
    get {
      complete{ BuildInfo.version }
    }
  }

  private def features = {
    get {
      complete { "features" }
    }
  }

  def retrieve(identifier : String)  = {
    get {
      complete (identifier)
    }
  }

  def search(query: String) = {
    get {
      complete {query}
    }
  }


}
