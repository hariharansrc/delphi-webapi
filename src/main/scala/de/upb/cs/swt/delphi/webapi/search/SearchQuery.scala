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

package de.upb.cs.swt.delphi.webapi.search

import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.search.SearchHits
import com.sksamuel.elastic4s.http.{ElasticClient, RequestSuccess}
import com.sksamuel.elastic4s.searches.queries.{NoopQuery, Query}
import de.upb.cs.swt.delphi.webapi.Configuration
import de.upb.cs.swt.delphi.webapi.artifacts.ArtifactTransformer
import de.upb.cs.swt.delphi.webapi.featuredefinitions.FeatureExtractor
import de.upb.cs.swt.delphi.webapi.search.querylanguage._

import scala.util.{Failure, Success, Try}

class SearchQuery(configuration: Configuration, featureExtractor: FeatureExtractor) {
  private val client = ElasticClient(configuration.elasticsearchClientUri)

  private def checkAndExecuteParsedQuery(ast: CombinatorialExpr, limit : Int): Try[SearchHits] = {
    val fields = collectFieldNames(ast)
    if (fields.diff(featureExtractor.featureList.toSeq).size > 0) return Failure(new IllegalArgumentException("Unknown field name used."))

    val query = searchWithType(configuration.esProjectIndex)
      .query(translate(ast))
      .sourceInclude(ArtifactTransformer.baseFields ++ fields.intersect(featureExtractor.featureList.toSeq).map(i => addPrefix(i)))
      .limit(limit)

    val response = client.execute {
      query
    }.await

    response match {
      case RequestSuccess(_, body, _, result) => Success(result.hits)
      case r => Failure(new IllegalArgumentException(r.toString))
    }
  }

  private def addPrefix(fieldName: String): String = s"hermes.features.$fieldName"

  private def translate(node: CombinatorialExpr): Query = {
    node match {
      case AndExpr(left, right) => bool {
        must(
          translate(left),
          translate(right)
        )
      }
      case OrExpr(left, right) => bool {
        should(
          translate(left),
          translate(right)
        )
      }
      case NotExpr(expr) => bool {
        not(translate(expr))
      }
      case XorExpr(left, right) => bool {
        should(
          must(
            translate(left),
            not(translate(right))
          ),
          must(
            not(translate(right)),
            translate(left)
          )
        )
      }
      case EqualExpr(field, value) => matchQuery(addPrefix(field.fieldName), value)
      case NotEqualExpr(field, value) => bool(not(matchQuery(addPrefix(field.fieldName), value)))
      case GreaterThanExpr(field, value) => rangeQuery(addPrefix(field.fieldName)).gt(value.toLong)
      case GreaterOrEqualExpr(field, value) => rangeQuery(addPrefix(field.fieldName)).gte(value.toLong)
      case LessThanExpr(field, value) => rangeQuery(addPrefix(field.fieldName)).lt(value.toLong)
      case LessOrEqualExpr(field, value) => rangeQuery(addPrefix(field.fieldName)).lte(value.toLong)
      case LikeExpr(field, value) => prefixQuery(addPrefix(field.fieldName), value)
      case _ => NoopQuery
    }
  }

  private def collectFieldNames(node: CombinatorialExpr): Seq[String] = {
    node match {
      case AndExpr(left, right) => collectFieldNames(left) ++ collectFieldNames(right)
      case OrExpr(left, right) => collectFieldNames(left) ++ collectFieldNames(right)
      case NotExpr(expr) => collectFieldNames(expr)
      case XorExpr(left, right) => collectFieldNames(left) ++ collectFieldNames(right)
      case EqualExpr(field, _) => Seq(field.fieldName)
      case NotEqualExpr(field, _) => Seq(field.fieldName)
      case GreaterThanExpr(field, _) => Seq(field.fieldName)
      case GreaterOrEqualExpr(field, _) => Seq(field.fieldName)
      case LessThanExpr(field, _) => Seq(field.fieldName)
      case LessOrEqualExpr(field, _) => Seq(field.fieldName)
      case LikeExpr(field, _) => Seq(field.fieldName)
      case IsTrueExpr(field) => Seq(field.fieldName)
      case FieldReference(name) => Seq(name)
      case _ => Seq()
    }
  }

  def search(query: QueryRequest) = {
    val parserResult = new Syntax(query.query).QueryRule.run()
    parserResult match {
      case Failure(e) => Failure(e)
      case Success(ast) => {
        checkAndExecuteParsedQuery(ast, query.limit.getOrElse(50)) match {
          case Failure(e) => Failure(e)
          case Success(hits) => Success(ArtifactTransformer.transformResults(hits))
        }
      }
    }
  }
}
