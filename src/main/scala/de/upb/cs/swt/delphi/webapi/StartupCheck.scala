package de.upb.cs.swt.delphi.webapi

import akka.actor.ActorSystem
import com.sksamuel.elastic4s.http.ElasticClient
import com.sksamuel.elastic4s.http.ElasticDsl._
import de.upb.cs.swt.delphi.instancemanagement.InstanceRegistry
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success, Try}

object StartupCheck  extends AppLogging {
   def check(configuration: Configuration)(implicit system: ActorSystem): Try[Configuration] = {
     log.warning("Performing Instance Registry checks")
     implicit val ec : ExecutionContext = system.dispatcher
    lazy val client = ElasticClient(configuration.elasticsearchClientUri)

    val f = (client.execute {
      nodeInfo()
    } map { i => {
      InstanceRegistry.sendMatchingResult(isElasticSearchReachable = true, configuration)
      Success(configuration)
    }
    } recover { case e =>
      InstanceRegistry.sendMatchingResult(isElasticSearchReachable = false, configuration)
      Failure(e)

    }).andThen {
      case _ => client.close()
    }

    Await.result(f, Duration.Inf)
  }
}
