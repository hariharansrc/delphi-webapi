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

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import spray.json._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

/**
  * @author Hariharan.
  */
class RequestLimitCheck extends WordSpec with Matchers with BeforeAndAfterAll with JsonSupport {
  val delphiRoutes = DelphiRoutes()
  val serverBinding: Future[Http.ServerBinding] = Http()
    .bindAndHandle(delphiRoutes, "localhost", 8085)

  override protected def beforeAll(): Unit = {
    serverBinding.onComplete {
      case Success(server) =>
        println(s"Server started at http://${server.localAddress.getHostString}:${server.localAddress.getPort}/")
      case Failure(e) =>
        e.printStackTrace()
        sys.exit(0)
    }
  }


  "Requests" should {
    "throttle when limit reached" in {
      def responseFuture: Future[HttpResponse] = Http()
        .singleRequest(HttpRequest(uri = "http://localhost:8085/version"))

      //Completing request limit
      for (i <- (1 to maxIndividualReq)) {
        Await.result(responseFuture, 1.second)
      }
      case class LimitMsg(msg: String)
      implicit val msgFormat = jsonFormat1(LimitMsg)

      val limitReachedFuture = responseFuture
      limitReachedFuture.onComplete {
        case Success(res) => {
          val msgPromise = Unmarshal(res.entity).to[LimitMsg]
          msgPromise.onComplete {
            case Success(limitMsg) => {
              assertResult("Request limit exceeded")(limitMsg.msg)
            }
            case Failure(exception) => {
              fail(exception)
            }
          }
        }
        case Failure(exception) => {
          fail(exception)
        }
      }
      Await.result(system.terminate(), 5.seconds)
    }
  }
}
