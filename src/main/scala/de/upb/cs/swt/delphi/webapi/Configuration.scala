package de.upb.cs.swt.delphi.webapi

import com.sksamuel.elastic4s.{ElasticsearchClientUri, IndexAndType}
import com.sksamuel.elastic4s.http.ElasticDsl._

/**
  * @author Ben Hermann
  */
class Configuration(  //Server and Elasticsearch configuration
                    val bindHost: String = "0.0.0.0",
                    val bindPort: Int = 8080,
                    val elasticsearchClientUri: ElasticsearchClientUri = ElasticsearchClientUri(
                      sys.env.getOrElse("DELPHI_ELASTIC_URI", "elasticsearch://localhost:9200")),
                    val esProjectIndex: IndexAndType = "delphi" / "project",

                      //Actor system configuration
                    val elasticActorPoolSize: Int = 8
                   ) {

}
