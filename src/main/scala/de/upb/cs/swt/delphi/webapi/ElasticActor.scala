package de.upb.cs.swt.delphi.webapi

import akka.actor.{Actor, ActorLogging, Props}
import com.sksamuel.elastic4s.IndexAndType
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.HttpClient
import de.upb.cs.swt.delphi.webapi.ElasticActorManager.{Enqueue, Retrieve}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class ElasticActor(configuration: Configuration, index: IndexAndType) extends Actor with ActorLogging{

  implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("elasticsearch-handling-dispatcher")
  val client = HttpClient(configuration.elasticsearchClientUri)

  override def preStart(): Unit = log.info("Search actor started")
  override def postStop(): Unit = log.info("Search actor shut down")
  context.setReceiveTimeout(2 seconds)

  override def receive = {
    case Enqueue(id) => getSource(id)
    case Retrieve(id) => getSource(id)
  }

  private def getSource(id: String) = {
    log.info("Executing get on entry {}", id)
    def source = client.execute{
      get(id).from(index)
    }.await match {
      case Right(res) => res.body.get
      case Left(_) => Option.empty
    }
    sender().tell(source, context.self)
  }
}

object ElasticActor{
  def props(configuration: Configuration, index: IndexAndType) : Props = Props(new ElasticActor(configuration, index))
    .withMailbox("es-priority-mailbox")
}
