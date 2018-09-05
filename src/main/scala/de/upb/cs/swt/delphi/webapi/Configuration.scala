package de.upb.cs.swt.delphi.webapi

import com.sksamuel.elastic4s.{ElasticsearchClientUri, IndexAndType}
import com.sksamuel.elastic4s.http.ElasticDsl._
import de.upb.cs.swt.delphi.instancemanagement.InstanceEnums.ComponentType
import de.upb.cs.swt.delphi.instancemanagement.{Instance, InstanceRegistry}

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


  lazy val elasticsearchClientUri: ElasticsearchClientUri = ElasticsearchClientUri({
    if(elasticsearchInstance.portnumber.isEmpty){
      elasticsearchInstance.iP.get
    }else{
      elasticsearchInstance.iP.get + ":" + elasticsearchInstance.portnumber.get
    }
  })

  lazy val elasticsearchInstance : Instance = InstanceRegistry.retrieveElasticSearchInstance(this) match {
    case Success(instance) => instance
    case Failure(_) => Instance(None, Some(sys.env.getOrElse("DELPHI_ELASTIC_URI","elasticsearch://localhost:9200")), None, Some("Default ElasticSearch instance"), Some(ComponentType.ElasticSearch) )
  }

  val instanceName = "MyWebApiInstance"
  val instanceRegistryUri : String = sys.env.getOrElse("DELPHI_IR_URI", "http://localhost:8085")
  lazy val usingInstanceRegistry = InstanceRegistry.register(this)

}
