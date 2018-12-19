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
package de.upb.cs.swt.delphi.webapi.search.elastic4s

import com.sksamuel.elastic4s.http.settings.IndexSettingsResponse
import com.sksamuel.elastic4s.http.{ElasticRequest, Handler}
import com.sksamuel.elastic4s.json.JacksonSupport
import de.upb.cs.swt.delphi.webapi.Configuration

package object extns {

  case class SettingsRequest(index: String, params: Map[String, Any])

  trait RichSettingsHandler {

    implicit object RichGetSettings extends Handler[SettingsRequest, IndexSettingsResponse] {

      override def build(request: SettingsRequest): ElasticRequest = {
        val endpoint = "/" + request.index + "/_settings"
        val req = ElasticRequest("GET", endpoint, request.params)
        req
      }
    }

  }

  def maxResultSize(res: Option[String], config: Configuration): Option[Int] = {
    res match {
      case Some(j) => {
        val custom = s"/${config.esIndex}/settings/index"
        val default = s"/${config.esIndex}/defaults/index"
        val target = "max_result_window"
        val node = JacksonSupport.mapper.readTree(j)
        val size = if (node.at(custom).has(target)) {
          Some(node.at(custom + "/" + target).asInt())
        }
        else {
          Some(node.at(default + "/" + target).asInt())
        }
        size
      }
      case None => {
        None
      }
    }
  }


  trait ElasticDslExtn extends RichSettingsHandler

  object ElasticDslExtn extends ElasticDslExtn

}
