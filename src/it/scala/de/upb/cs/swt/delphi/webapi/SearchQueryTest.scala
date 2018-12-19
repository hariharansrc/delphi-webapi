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

import de.upb.cs.swt.delphi.webapi.search.{QueryRequest, SearchError, SearchQuery}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Failure

class SearchQueryTest extends FlatSpec with Matchers {
  "Search query" should "fail on large request limit" in {
    val configuration = new Configuration()
    val q = new SearchQuery(configuration, new FeatureQuery(configuration))
    val size = 20000
    val response = q.search(QueryRequest("[dstore_1 (opcode:72)]<1", Some(size)))
    response match {
      case Failure(exception) => {
        exception shouldBe a[SearchError]
      }
      case _ => {
        fail("Limit exceeded should fail")
      }
    }
  }
}
