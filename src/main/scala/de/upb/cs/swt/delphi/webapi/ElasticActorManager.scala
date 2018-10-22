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

import akka.actor.{Actor, ActorLogging, Props, Terminated}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import de.upb.cs.swt.delphi.webapi.ElasticActorManager.ElasticMessage

class ElasticActorManager(configuration: Configuration) extends Actor with ActorLogging{

  private val index = configuration.esProjectIndex
  private var elasticRouter = {
    val routees = Vector.fill(configuration.elasticActorPoolSize) {
      val r = context.actorOf(ElasticActor.props(configuration, index))
      context watch r
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  override def preStart(): Unit = log.info("Actor manager started")
  override def postStop(): Unit = log.info("Actor manager shut down")

  override def receive = {
    case em: ElasticMessage => {
      log.info("Forwarding request {} to ElasticActor", em)
      elasticRouter.route(em, sender())
    }
    case Terminated(id) => {
      elasticRouter.removeRoutee(id)
      val r = context.actorOf(ElasticActor.props(configuration, index))
      context watch r
      elasticRouter = elasticRouter.addRoutee(r)
    }
  }
}

object ElasticActorManager{
  def props(configuration: Configuration) : Props = Props(new ElasticActorManager(configuration))
    .withMailbox("es-priority-mailbox")

  sealed trait ElasticMessage

  final case class Retrieve(id: String) extends ElasticMessage
  final case class Enqueue(id: String) extends ElasticMessage
}