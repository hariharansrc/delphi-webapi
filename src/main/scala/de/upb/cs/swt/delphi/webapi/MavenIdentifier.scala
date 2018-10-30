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

import java.net.{URI, URLEncoder}
import java.nio.charset.StandardCharsets

import de.upb.cs.swt.delphi.webapi.artifacts.Identifier

case class MavenIdentifier(val repository: Option[String], val groupId: String, val artifactId: String, val version: Option[String]) extends Identifier {

  def toUniqueString = {
    repository + ":" + groupId + ":" + artifactId + ":" + version
  }

  override val toString: String = groupId + ":" + artifactId + ":" + version

  def toJarLocation : URI = {
    constructArtifactBaseUri().resolve(encode(artifactId) + "-" + encode(version.getOrElse("")) + ".jar")
  }

  def toPomLocation : URI = {
    constructArtifactBaseUri().resolve(encode(artifactId) + "-" + encode(version.getOrElse("")) + ".pom")
  }

  private def constructArtifactBaseUri(): URI =
    new URI(repository.getOrElse(""))
      .resolve(encode(groupId).replace('.', '/') + "/")
      .resolve(encode(artifactId) + "/")
      .resolve(encode(version.getOrElse("")) + "/")

  private def encode(input : String) : String =
    URLEncoder.encode(input, StandardCharsets.UTF_8.toString())
}

object MavenIdentifier {
  private implicit def wrapOption[A](value : A) : Option[A] = Some(value)
  def apply(s: String): Option[MavenIdentifier] = {
    val splitString: Array[String] = s.split(':')
    if (splitString.length < 2 || splitString.length > 3) return None
    //if (splitString(0).startsWith("http")) {
    //  if (splitString.length < 3) return None
    //  MavenIdentifier(splitString(0), splitString(1), splitString(2), if (splitString.length < 4) None else splitString(3))
    //} else {
      //if (splitString.length > 3) return None
      MavenIdentifier(None, splitString(0), splitString(1), if (splitString.length < 3) None else splitString(2))
    //}
  }
}