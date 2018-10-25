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

import com.sksamuel.elastic4s.http.{ElasticClient, RequestSuccess}
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.search.SearchResponse
import spray.json.DefaultJsonProtocol

class StatisticsQuery(configuration: Configuration) {
  def retrieveStandardStatistics = {
    val client = ElasticClient(configuration.elasticsearchClientUri)

    val fullIndexSize = searchWithType(configuration.esProjectIndex) size 0
    val hermesEnabledProjects = searchWithType(configuration.esProjectIndex) query bool {
      must {
        existsQuery("hermes")
      }
    } size (0)


    client.execute {
      multi (
        fullIndexSize,
        hermesEnabledProjects
      )
    }.await match {
        // TODO: These matchers are non exhaustive
      case RequestSuccess(_, _, _, results) => {
        assert(results.size == 2)
        val total = results.items(0).response match {
          case s: SearchResponse => {
            s.hits.total
          }
        }
        val hermesTotal = results.items(1).response match {
          case s: SearchResponse => {
            s.hits.total
          }
        }
        Statistics(hermesTotal, hermesTotal)
      }
    }
  }
}

case class Statistics(total : Long, hermesEnabled : Long)

object StatisticsJson extends DefaultJsonProtocol {
  implicit val statisticsFormat = jsonFormat2(Statistics)
}
