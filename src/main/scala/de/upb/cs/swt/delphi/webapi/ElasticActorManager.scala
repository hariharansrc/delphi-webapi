package de.upb.cs.swt.delphi.webapi

import akka.actor.{Actor, ActorLogging, Props, Terminated}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import de.upb.cs.swt.delphi.webapi.ElasticActorManager.{Enqueue, Retrieve}
import de.upb.cs.swt.delphi.webapi.ElasticActor.GetSource

class ElasticActorManager(configuration: Configuration) extends Actor with ActorLogging{

  private val index = configuration.esProjectIndex
  private var elasticRouter = {
    val routees = Vector.fill(configuration.elasticActorPoolSize) {
      val r = context.actorOf(ElasticActor.props(configuration))
      context watch r
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  override def preStart(): Unit = log.info("Actor manager started")
  override def postStop(): Unit = log.info("Actor manager shut down")

  override def receive = {
    case Retrieve(id) => getSource(id)
    case Enqueue(id) =>  getSource(id)
    case Terminated(id) => {
      elasticRouter.removeRoutee(id)
      val r = context.actorOf(ElasticActor.props(configuration))
      context watch r
      elasticRouter = elasticRouter.addRoutee(r)
    }
  }

  private def getSource(id: String) = {
    log.info("Forwarding search for entry {}", id)
    elasticRouter.route(GetSource(id, index), sender())
  }
}

object ElasticActorManager{
  def props(configuration: Configuration) : Props = Props(new ElasticActorManager(configuration))
    .withMailbox("es-priority-mailbox")

  sealed trait ElasticMessage

  final case class Retrieve(id: String) extends ElasticMessage
  final case class Enqueue(id: String) extends ElasticMessage
}