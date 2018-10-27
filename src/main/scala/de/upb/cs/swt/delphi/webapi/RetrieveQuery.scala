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

import com.sksamuel.elastic4s.http.{ElasticClient, RequestSuccess, Response}
import com.sksamuel.elastic4s.http.ElasticDsl.{termQuery, _}
import com.sksamuel.elastic4s.http.search.{SearchHit, SearchHits, SearchResponse}
import com.sksamuel.elastic4s.searches.queries.Query
import com.sksamuel.elastic4s.searches.queries.term.TermQuery
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, RootJsonFormat}

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
                Some(transformResults(results.hits))
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

  private def getHermesResults(sourceMap: Map[String, AnyRef]): Map[String, Int] = {
    if (!sourceMap.contains("hermes")) return Map()
    if (!sourceMap("hermes").isInstanceOf[Map[String, AnyRef]]) return Map()
    if (!sourceMap("hermes").asInstanceOf[Map[String, AnyRef]].contains("features")) return Map()

    val hermesMap = sourceMap("hermes").asInstanceOf[Map[String, AnyRef]]

    hermesMap("features").asInstanceOf[Map[String, Int]]
  }

  def getMetadata(sourceMap: Map[String, AnyRef]): ArtifactMetadata = {
    val identifier = sourceMap("identifier").asInstanceOf[Map[String, String]]
    ArtifactMetadata(sourceMap("source").asInstanceOf[String],
      ISODateTimeFormat.dateTime().parseDateTime(sourceMap("discovered").asInstanceOf[String]),
      identifier("groupId"), identifier("artifactId"), identifier("version"))
  }

  private def transformResult(h: SearchHit) = {
    val sourceMap = h.sourceAsMap
    Artifact(h.id, getMetadata(sourceMap), getHermesResults(sourceMap))
  }

  private def transformResults(hits: SearchHits) = {
    hits.hits.map(h => transformResult(h))
  }
}

case class Artifact(id: String, metadata: ArtifactMetadata, metricResults: Map[String, Int])

case class ArtifactMetadata(source: String, discovered: DateTime, groupId: String, artifactId: String, version: String)

object ArtifactJson extends DefaultJsonProtocol {

  implicit object DateJsonFormat extends RootJsonFormat[DateTime] {

    private val parserISO: DateTimeFormatter = ISODateTimeFormat.dateTime()

    override def write(obj: DateTime) = JsString(parserISO.print(obj))

    override def read(json: JsValue): DateTime = json match {
      case JsString(s) => parserISO.parseDateTime(s)
      case _ => throw new DeserializationException("Error info you want here ...")
    }
  }

  implicit val artifactMetadataFormat = jsonFormat5(ArtifactMetadata)
  implicit val artifactFormat = jsonFormat3(Artifact)

  def prettyPrint(pretty: Option[_], value: JsValue): String = {
    pretty.isDefined match {
      case true => value.sortedPrint
      case false => value.compactPrint
    }
  }
}

