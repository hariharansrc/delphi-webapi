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
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.upb.cs.swt.delphi.featuredefinitions.FeatureListMapping
import de.upb.cs.swt.delphi.instancemanagement.InstanceRegistry
import de.upb.cs.swt.delphi.webapi.ElasticActorManager.{Enqueue, Retrieve}
import de.upb.cs.swt.delphi.webapi.ElasticRequestLimiter.Validate
import spray.json._

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

/**
  * Web server configuration for Delphi web API.
  */
object Server extends HttpApp with JsonSupport with AppLogging {

  private val configuration = new Configuration()
  implicit val system = ActorSystem("delphi-webapi")
  private val actorManager = system.actorOf(ElasticActorManager.props(configuration))
  private val requestLimiter = system.actorOf(ElasticRequestLimiter.props(configuration, actorManager))
  implicit val timeout = Timeout(5, TimeUnit.SECONDS)
  implicit val materializer = ActorMaterializer()

  override def routes: Route =
    path("version") { version } ~
      path("features") { features } ~
      path("statistics") { statistics } ~
      pathPrefix("search" / Remaining) { query => search(query) } ~
      pathPrefix("retrieve" / Remaining) { identifier => retrieve(identifier) }
      //~
      //pathPrefix("enqueue" / Remaining) { identifier => enqueue(identifier) }


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
        FeatureListMapping.featureList.toJson
      }
    }
  }

  private def statistics = {
    get {
      complete {
        import StatisticsJson._
        new StatisticsQuery(configuration).retrieveStandardStatistics.toJson
      }
    }
  }

  private def retrieve(identifier: String): Route = {
    get {
      pass {
        complete(
          (actorManager ? Retrieve(identifier)).mapTo[String]
        )
      } ~ extractClientIP { ip =>
        complete(
          (requestLimiter ? Validate(ip, Retrieve(identifier))).mapTo[String]
        )
      }
    }
  }

  def enqueue(identifier: String): Route = {
    get {
      pass { //TODO: Require authorization here
        complete(
          (actorManager ? Enqueue(identifier)).mapTo[String]
        )
      }
    }
  }

  def search(query: String): Route = {
    get {
      complete {
        query
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

    implicit val ec: ExecutionContext = ExecutionContext.global
    val terminationFuture = system.terminate()

    terminationFuture.onComplete {
      sys.exit()
    }
  }


}


