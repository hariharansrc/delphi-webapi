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

import com.sksamuel.elastic4s.http.ElasticDsl.{termQuery, _}
import com.sksamuel.elastic4s.http.search.SearchResponse
import com.sksamuel.elastic4s.http.{ElasticClient, RequestSuccess, Response}
import com.sksamuel.elastic4s.searches.queries.Query
import com.sksamuel.elastic4s.searches.queries.term.TermQuery
import de.upb.cs.swt.delphi.webapi.artifacts.ArtifactTransformer

object RetrieveQuery {
  def retrieve(identifier: String)(implicit configuration: Configuration) = {
    val client = ElasticClient(configuration.elasticsearchClientUri)

    val parsedIdentifier = MavenIdentifier(identifier)


    parsedIdentifier match {
      case None => None
      case Some(m) => {
        val response: Response[SearchResponse] = client.execute {
          searchWithType(configuration.esProjectIndex) query {
            bool {
              must(
                constructIdQuery(m)
              )
            }
          }
        }.await

        response match {
          case RequestSuccess(_, body, _, results: SearchResponse) => {
            results.totalHits match {
              case 0L => None
              case _ => {
                Some(ArtifactTransformer.transformResults(results.hits))
              }
            }
          }
          case _ => None
        }
      }
    }


  }

  private def constructIdQuery(m: MavenIdentifier): Iterable[Query] = {
    val baseQuery: List[TermQuery] = List(
      termQuery("identifier.groupId", m.groupId),
      termQuery("identifier.artifactId", m.artifactId)
    )
    m.version.isDefined match {
      case true => {
        baseQuery.+:(termQuery("identifier.version", m.version.getOrElse("")))
      }
      case false => {
        baseQuery
      }
    }
  }

}







