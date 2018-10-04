
// Copyright (C) 2018 The Delphi Team.
// See the LICENCE file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

// http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package de.upb.cs.swt.delphi.webapi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.sksamuel.elastic4s.RefreshPolicy
import com.sksamuel.elastic4s.embedded.LocalNode
import com.sksamuel.elastic4s.http.ElasticDsl._
import org.elasticsearch.common.settings.Settings
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

/**
  * @author Hariharan.
  */
class ElasticActorTest extends FlatSpec with Matchers with BeforeAndAfterAll {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()(system)
  implicit val executionContext = system.dispatcher
  val node = LocalNode("elasticsearch","local-path")
  val client = node.client(shutdownNodeOnClose = true)

  override def beforeAll(): Unit = {
    client.execute {
      createIndex("delphi").mappings(
        mapping("project").fields(
          keywordField("name"),
          keywordField("source"),
          keywordField("identifier.groupId"),
          keywordField("identifier.artifactId"),
          keywordField("identifier.version")
        )
      )
    }.await

    client.execute {
      indexInto("delphi" / "project").fields(
        "name" -> "yom:yom:1.0-alpha-2"
      ).refresh(RefreshPolicy.IMMEDIATE)
    }.await
  }

  override def afterAll(): Unit = {
    super.afterAll()
    client.close()
    system.terminate();
  }



  "Version no.." should
    "match version from build.sbt" in {
    val res: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://localhost:8080/version"))
    res.onComplete {
      case Success(ver) => {
        assert(ver.status.isSuccess());
        val res2Str: Future[String] = Unmarshal(ver.entity).to[String]
        res2Str.onComplete {
          case Success(value) => {
            assert(value.equals(BuildInfo.version))
          }
          case Failure(e) => {
            assertThrows(e);
          }
        }
      }
      case Failure(e) => {
        assertThrows(e);
      }
    }
    Await.result(res, 5.seconds)
  }

  "Retrive endpoint" should
    "get yom:yom:1.0-alpha-2 artifact" in {
    val mavenId = "yom:yom:1.0-alpha-2"
    val url = s"http://localhost:8080/retrieve/${mavenId}"
    val res: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = url))
    res.onComplete {
      case Success(data) => {
        assert(data.status.isSuccess())
        val res2Str: Future[String] = Unmarshal(data.entity).to[String]
        println(res2Str)
      }
      case Failure(exception) => {
        assertThrows(exception)
      }
    }
  }

}
