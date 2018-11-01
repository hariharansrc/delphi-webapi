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

package de.upb.cs.swt.delphi.webapi.artifacts

import com.sksamuel.elastic4s.http.search.{SearchHit, SearchHits}
import org.joda.time.format.ISODateTimeFormat

object ArtifactTransformer {

  private def getHermesResults(sourceMap: Map[String, AnyRef]): Map[String, Int] = {
    if (!sourceMap.contains("hermes")) return Map()
    if (!sourceMap("hermes").isInstanceOf[Map[String, AnyRef]]) return Map()
    if (!sourceMap("hermes").asInstanceOf[Map[String, AnyRef]].contains("features")) return Map()

    val hermesMap = sourceMap("hermes").asInstanceOf[Map[String, AnyRef]]

    hermesMap("features").asInstanceOf[Map[String, Int]]
  }

  private def getMetadata(sourceMap: Map[String, AnyRef]): ArtifactMetadata = {
    val identifier = sourceMap("identifier").asInstanceOf[Map[String, String]]
    ArtifactMetadata(sourceMap("source").asInstanceOf[String],
      ISODateTimeFormat.dateTime().parseDateTime(sourceMap("discovered").asInstanceOf[String]),
      identifier("groupId"), identifier("artifactId"), identifier("version"))
  }

  def transformResult(id : String, sourceMap : Map[String, AnyRef]) :Artifact = {
    Artifact(id, getMetadata(sourceMap), getHermesResults(sourceMap))
  }


  private def transformResult(h: SearchHit): Artifact = {
    transformResult(h.id, h.sourceAsMap)
  }


  def transformResults(hits: SearchHits): Array[Artifact] = {
    hits.hits.map(h => transformResult(h))
  }

  val baseFields = Seq("source", "discovered", "identifier.groupId", "identifier.artifactId", "identifier.version")
}
