package de.upb.cs.swt.delphi.webapi

import com.sksamuel.elastic4s.{ElasticsearchClientUri, IndexAndType}
import com.sksamuel.elastic4s.http.ElasticDsl._
import de.upb.cs.swt.delphi.instancemanagement.InstanceEnums.ComponentType
import de.upb.cs.swt.delphi.instancemanagement.{Instance, InstanceRegistry}

import scala.util.{Failure, Success, Try}

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


  lazy val elasticsearchClientUri: ElasticsearchClientUri = ElasticsearchClientUri(
    elasticsearchInstance.host + ":" + elasticsearchInstance.portnumber)

  lazy val elasticsearchInstance : Instance = InstanceRegistry.retrieveElasticSearchInstance(this) match {
    case Success(instance) => instance
    case Failure(_) => Instance(
      None,
      fallbackElasticSearchHost,
      fallbackElasticSearchPort,
      "Default ElasticSearch instance",
      ComponentType.ElasticSearch)
  }
  val defaultElasticSearchPort : Int = 9200
  val defaultElasticSearchHost : String = "elasticsearch://localhost"
  val instanceName = "MyWebApiInstance"
  val instanceRegistryUri : String = sys.env.getOrElse("DELPHI_IR_URI", "http://localhost:8087")
  lazy val usingInstanceRegistry : Boolean = assignedID match {
    case Some(_) => true
    case None => false
  }
  lazy val assignedID : Option[Long] = InstanceRegistry.register(this) match {
    case Success(id) => Some(id)
    case Failure(_) => None
  }
  lazy val fallbackElasticSearchPort : Int = sys.env.get("DELPHI_ELASTIC_URI") match {
    case Some(hostString) => if(hostString.count(c => c == ':') == 3){
      Try(hostString.split(":")(2).toInt) match {
        case Success(port) => port
        case Failure(_) => defaultElasticSearchPort
      }
    } else {
      defaultElasticSearchPort
    }
    case None => defaultElasticSearchPort
  }

  lazy val fallbackElasticSearchHost : String = sys.env.get("DELPHI_ELASTIC_URI") match {
    case Some(hostString) =>
      if(hostString.count(c => c == ':') == 2){
        hostString.substring(0,hostString.lastIndexOf(":"))
      } else {
        defaultElasticSearchHost
      }
    case None => defaultElasticSearchHost

  }

}


