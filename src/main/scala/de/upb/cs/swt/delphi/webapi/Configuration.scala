package de.upb.cs.swt.delphi.webapi

import com.sksamuel.elastic4s.ElasticsearchClientUri

/**
  * @author Ben Hermann
  */
class Configuration(val bindHost: String = "0.0.0.0",
                    val bindPort: Int = 8080,
                    val elasticsearchClientUri: ElasticsearchClientUri = ElasticsearchClientUri(
                      sys.env.getOrElse("DELPHI_ELASTIC_URI", "elasticsearch://localhost:9200"))) {

}
