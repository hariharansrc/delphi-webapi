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

package de.upb.cs.swt.delphi.webapi.featuredefinitions

import com.sksamuel.elastic4s.http.{ElasticClient, RequestSuccess}
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.index.mappings.IndexMappings
import de.upb.cs.swt.delphi.webapi.Configuration
import org.slf4j.LoggerFactory
import spray.json._

class FeatureExtractor(configuration: Configuration) {
  private val log = LoggerFactory.getLogger(this.getClass)

  lazy val featureList: Iterable[String] = {
    val client = ElasticClient(configuration.elasticsearchClientUri)
    val mappingRequest = client.execute {
      getMapping(configuration.esProjectIndex)
    }.await

    mappingRequest match {
      case RequestSuccess(_, body, _, mappings) => {
        val bodyJson = body.getOrElse("").parseJson.asJsObject

        bodyJson
          .fields("delphi").asJsObject
          .fields("mappings").asJsObject
          .fields("project").asJsObject
          .fields("properties").asJsObject
          .fields("hermes").asJsObject
          .fields("properties").asJsObject
          .fields("features").asJsObject
          .fields("properties").asJsObject.fields.keys
      }
      case _ => {
        log.warn(s"Could not retrieve current feature list. Error was: $mappingRequest")
        List()
      }
    }
  }
}
