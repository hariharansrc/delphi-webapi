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

package de.upb.cs.swt.delphi

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


package object webapi {

  implicit val system: ActorSystem = ActorSystem("delphi-webapi")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  implicit val configuration: Configuration = new Configuration()

  val defaultTimeout = 5
  implicit val timeout: Timeout = Timeout(defaultTimeout, TimeUnit.SECONDS)

  /**
    * Maximum no of requests allowed until `refreshRate` is triggered.
    */
  val maxTotalNoRequest = 2000
  /**
    * Maximum no of requests allowed for an individual until `refreshRate` is triggered.
    */
  val maxIndividualReq = 200
  /**
    * Used by `Source.tick` to refresh ip log periodically
    */
  val refreshRate: FiniteDuration = 5.minutes
}
