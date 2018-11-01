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

import org.scalatest.{FlatSpec, Matchers}

class MavenIdentifierApplyTest extends FlatSpec with Matchers {
  "Valid identifiers with version" should "convert nicely" in {
    val fullIdentifier = MavenIdentifier("log4j:log4j-test:1.4.2")
    fullIdentifier match {
      case Some(m) => {
        m.repository.isDefined shouldBe false
        m.groupId shouldBe "log4j"
        m.artifactId shouldBe "log4j-test"
        m.version.isDefined shouldBe true
        m.version.getOrElse("") shouldBe "1.4.2"
      }
      case _ => fail("Identifier could not be properly parsed.")
    }
  }
  "Valid identifiers without version" should "convert nicely" in {
    val partial = MavenIdentifier("log4j:log4j-test")
    partial match {
      case Some(m) => {
        m.repository.isDefined shouldBe false
        m.groupId shouldBe "log4j"
        m.artifactId shouldBe "log4j-test"
        m.version.isDefined shouldBe false
      }
      case _ => fail("Identifier could not be properly parsed.")
    }
  }

  "Invalid identifiers" should "fail" in {
    val invalid = MavenIdentifier("log4j")
    invalid.isDefined shouldBe false

    val invalid2 = MavenIdentifier("log4j:::")
    invalid2.isDefined shouldBe false

    val invalid3 = MavenIdentifier("log4j::")
    invalid3.isDefined shouldBe false

    val invalid4 = MavenIdentifier("")
    invalid4.isDefined shouldBe false
  }
}
