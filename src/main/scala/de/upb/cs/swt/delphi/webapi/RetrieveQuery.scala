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

import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.get.GetResponse
import com.sksamuel.elastic4s.http.{ElasticClient, RequestSuccess, Response}
import de.upb.cs.swt.delphi.webapi.artifacts.{Artifact, ArtifactTransformer}

object RetrieveQuery {

  def retrieve(identifier: String)(implicit configuration: Configuration): Option[List[Artifact]] = {
    val client = ElasticClient(configuration.elasticsearchClientUri)

    val parsedIdentifier = MavenIdentifier(identifier)

    parsedIdentifier match {
      case None => None
      case Some(m) => {

        val response: Response[GetResponse] = client.execute {
          get(configuration.esProjectIndex.index, configuration.esProjectIndex.`type`, m.toUniqueString)
        }.await

        response match {
          case RequestSuccess(_, body, _, results: GetResponse) => {
            results.found match {
              case false => None
              case _ => {
                Some(List(ArtifactTransformer.transformResult(results.id, results.sourceAsMap)))
              }
            }
          }
          case _ => None
        }
      }
    }
  }
}







