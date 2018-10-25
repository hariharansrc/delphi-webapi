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

import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.upb.cs.swt.delphi.featuredefinitions.FeatureListMapping
import org.scalatest.{Matchers, WordSpec}

class ServerTests extends WordSpec with Matchers with ScalatestRouteTest
{

  "The Server" should { "return the version number" in {
    Get("version")-> Server.routes -> check{
      responseAs[String] shouldEqual(BuildInfo.version)
    } }

    "display features list" in {
      Get("features") -> Server.routes -> check{
          responseAs[String] shouldEqual(FeatureListMapping.featureList)
    }}
    "return Method not allowed error" in {
      Get("version") -> Server.routes -> check{
        status === StatusCodes.METHOD_NOT_ALLOWED
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: GET"
      }
      Get("features") -> Server.routes -> check{
        status === StatusCodes.METHOD_NOT_ALLOWED
        responseAs[String] shouldEqual "Http method not allowed, supported methods: GET"
      }
      Get("retrieve") -> Server.routes -> check{
        status === StatusCodes.METHOD_NOT_ALLOWED
        responseAs[String] shouldEqual "Http method not allowed, supported methods: GET"
      }
      Get("enqueue") -> Server.routes -> check{
        status === StatusCodes.METHOD_NOT_ALLOWED
        responseAs[String] shouldEqual "Http method not allowed, supported methods: GET"
      }
      Get("search") -> Server.routes -> check{
        status === StatusCodes.METHOD_NOT_ALLOWED
        responseAs[String] shouldEqual "Http method not allowed, supported methods: GET"
      }
      Get("stop") -> Server.routes -> check{
        status === StatusCodes.METHOD_NOT_ALLOWED
        responseAs[String] shouldEqual "Http method not allowed, supported methods: POST"
      }
    }
  }
}