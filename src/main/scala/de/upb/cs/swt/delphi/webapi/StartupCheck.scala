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

import akka.actor.ActorSystem
import com.sksamuel.elastic4s.http.ElasticClient
import com.sksamuel.elastic4s.http.ElasticDsl._
import de.upb.cs.swt.delphi.instancemanagement.InstanceRegistry
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success, Try}

object StartupCheck  extends AppLogging {
   def check(configuration: Configuration)(implicit system: ActorSystem): Try[Configuration] = {
     log.warning("Performing Instance Registry checks")
     implicit val ec : ExecutionContext = system.dispatcher
    lazy val client = ElasticClient(configuration.elasticsearchClientUri)

    val f = (client.execute {
      nodeInfo()
    } map { i => {
      InstanceRegistry.sendMatchingResult(isElasticSearchReachable = true, configuration)
      Success(configuration)
    }
    } recover { case e =>
      InstanceRegistry.sendMatchingResult(isElasticSearchReachable = false, configuration)
      Failure(e)

    }).andThen {
      case _ => client.close()
    }

    Await.result(f, Duration.Inf)
  }
}
