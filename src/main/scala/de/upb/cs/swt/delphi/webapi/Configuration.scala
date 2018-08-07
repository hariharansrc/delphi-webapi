package de.upb.cs.swt.delphi.webapi

import com.sksamuel.elastic4s.{ElasticsearchClientUri, IndexAndType}
import com.sksamuel.elastic4s.http.ElasticDsl._
import de.upb.cs.swt.delphi.instancemanagement.InstanceRegistry

import scala.util.{Failure, Success}

/**
  * @author Ben Hermann
  */
class Configuration(  //Server and Elasticsearch configuration
                    val bindHost: String = "0.0.0.0",
                    val bindPort: Int = 8080,
                    val esProjectIndex: IndexAndType = "delphi" / "project",

                      //Actor system configuration
                    val elasticActorPoolSize: Int = 8
                   ) {


  lazy val elasticsearchClientUri: ElasticsearchClientUri = ElasticsearchClientUri(InstanceRegistry.retrieveElasticSearchInstance(this) match {
    case Success(elasticIP) => elasticIP
    case Failure(_) => sys.env.getOrElse("DELPHI_ELASTIC_URI","elasticsearch://localhost:9200")
  })

  val instanceRegistryUri : String = sys.env.getOrElse("DELPHI_IR_URI", "http://localhost:9300")

  lazy val usingInstanceRegistry = InstanceRegistry.register("MyWebApiInstance",this) match {
    case Success(_) => true
    case Failure(_) => {
      println(s"Failed to connect to Instance Registry at ${instanceRegistryUri}. Using default configuration instead.")
      false
    }
  }
}
