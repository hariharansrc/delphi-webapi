// Copyright (C) 2018 The Delphi Team.
// See the LICENCE file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package de.upb.cs.swt.delphi.webapi

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.upb.cs.swt.delphi.instancemanagement.InstanceRegistry
import de.upb.cs.swt.delphi.webapi.artifacts.ArtifactJson._
import de.upb.cs.swt.delphi.webapi.search.QueryRequestJson._
import de.upb.cs.swt.delphi.webapi.search.{QueryRequest, SearchQuery}
import spray.json._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
  * Web server configuration for Delphi web API.
  */
object Server extends HttpApp with JsonSupport with AppLogging {

  implicit val system = ActorSystem("delphi-webapi")
  implicit val materializer = ActorMaterializer()

  private implicit val configuration = new Configuration()
  private implicit val timeout = Timeout(5, TimeUnit.SECONDS)


  override def routes: Route =
    path("version") {
      version
    } ~
      path("features") {
        features
      } ~
      path("statistics") {
        statistics
      } ~
      pathPrefix("search" ) { search } ~
      pathPrefix("retrieve" / Remaining) { identifier => retrieve(identifier) }


  private def version = {
    get {
      complete {
        BuildInfo.version
      }
    }
  }

  private val featureExtractor = new FeatureQuery(configuration)

  private def features = {
    get {
      parameter('pretty.?) { (pretty) =>
        complete(
          //TODO: Introduce failure concept for feature extractor
          prettyPrint(pretty, featureExtractor.featureList.toJson)
        )
      }
    }
  }

  private def statistics = {
    get {
      parameter('pretty.?) { (pretty) =>
        complete {
          val result = new StatisticsQuery(configuration).retrieveStandardStatistics
          result match {
            case Some(stats) => {
              import StatisticsJson._
              prettyPrint(pretty, stats.toJson)
            }
            case _ => HttpResponse(StatusCodes.InternalServerError)
          }
        }
      }
    }
  }

  private def retrieve(identifier: String): Route = {
    get {
      parameter('pretty.?) { (pretty) =>
        complete(
          RetrieveQuery.retrieve(identifier) match {
            case Some(result) => prettyPrint(pretty, result.toJson)
            case None => HttpResponse(StatusCodes.NotFound)
          }
        )
      }
    }
  }

  def search: Route = {
    post {
      parameter('pretty.?) { (pretty) =>
        entity(as[QueryRequest]) { input =>
          log.info(s"Received search query: ${input.query}")
          complete(
            new SearchQuery(configuration, featureExtractor).search(input) match {
              case Success(result) => prettyPrint(pretty, result.toJson)
              case Failure(e) => e.getMessage
            }
          )
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {
    sys.addShutdownHook({
      log.warning("Received shutdown signal.")
      InstanceRegistry.handleInstanceStop(configuration)
    })

    StartupCheck.check(configuration)
    Server.startServer(configuration.bindHost, configuration.bindPort, system)

    implicit val ec: ExecutionContext = system.dispatcher
    val terminationFuture = system.terminate()

    terminationFuture.onComplete {
      sys.exit(0)
    }
  }


}


