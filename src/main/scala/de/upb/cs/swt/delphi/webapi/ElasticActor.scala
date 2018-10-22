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
import akka.actor.{Actor, ActorLogging, Props}
import com.sksamuel.elastic4s.IndexAndType
import com.sksamuel.elastic4s.http.{ElasticClient, RequestFailure, RequestSuccess}
import com.sksamuel.elastic4s.http.ElasticDsl._
import de.upb.cs.swt.delphi.webapi.ElasticActorManager.{Enqueue, Retrieve}

import spray.json._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class ElasticActor(configuration: Configuration, index: IndexAndType) extends Actor with ActorLogging {

  implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("elasticsearch-handling-dispatcher")
  val client = ElasticClient(configuration.elasticsearchClientUri)

  override def preStart(): Unit = log.info("Search actor started")

  override def postStop(): Unit = log.info("Search actor shut down")

  context.setReceiveTimeout(2 seconds)

  override def receive = {
    case Enqueue(id) => getSource(id)
    case Retrieve(id) => getSource(id)
  }

  private def getSource(id: String) = {
    log.info("Executing get on entry {}", id)
    val searchByName = searchWithType(index) query must(
      matchQuery("name", id)
    )
    log.info(s"Query {}",client.show(searchByName))
    def queryResponse = client.execute {
      log.info(s"Got retrieve request for $id.")
      searchByName
    }.await

    val source = queryResponse match {
      case results: RequestSuccess[_] => {
        val resObj = results.body.get.parseJson.asJsObject
        val hitsObj=resObj.fields.getOrElse("hits", JsObject.empty).asJsObject
        val hitsArr=hitsObj.fields.getOrElse("hits",JsArray.empty).asInstanceOf[JsArray]
        val source=hitsArr.elements.map(m=>m.asJsObject.fields.get("_source"))
        source.head.getOrElse(JsObject.empty).toString()
      }
      case failure: RequestFailure => Option.empty
    }
    sender().tell(source, context.self)
  }
}

object ElasticActor {
  def props(configuration: Configuration, index: IndexAndType): Props = Props(new ElasticActor(configuration, index))
    .withMailbox("es-priority-mailbox")
}
