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

package de.upb.cs.swt.delphi.webapi.search.querylanguage

import org.scalatest.{FlatSpec, Matchers}
import scala.util.{Failure, Success}

/**
  * Tests for the DelphiQL syntax.
  *
  * @author Lisa Nguyen Quang Do
  */
class SyntaxTest extends FlatSpec with Matchers {

  "Syntax.singularConditionWithOperator" should "be valid" in {
    val parseResult = new Syntax("[Filter1]=abc").QueryRule.run()
    parseResult shouldBe a [Success[_]]
    parseResult match {
      case Success(ast) => {
       ast.toString shouldEqual "EqualExpr(FieldReference(Filter1),abc)"
      }
    }
  }


  "Syntax.singularConditionNoOperator" should "be valid" in {
    val parseResult = new Syntax("[Filter1]").QueryRule.run()
    parseResult shouldBe a [Success[_]]
    parseResult match {
      case Success(ast) => {
        ast.toString shouldEqual "IsTrueExpr(FieldReference(Filter1))"
      }
    }
  }


  "Syntax.singularConditionTypo" should "be valid" in {
    val parseResult = new Syntax("[Filter1]=<3").QueryRule.run()
    parseResult shouldBe a [Failure[_]]
  }

  "Syntax.singularConditionOddCharacters" should "be valid" in {
    val parseResult = new Syntax("[Filter1]%[Filter2]%'}.:").QueryRule.run()
    parseResult shouldBe a [Failure[_]]
  }


  "Syntax.combinatoryConditionSimple" should "be valid" in {
    val parseResult = new Syntax("[Filter1]&&[Filter2]=3").QueryRule.run()
    parseResult shouldBe a [Success[_]]
    parseResult match {
      case Success(ast) => {
        ast.toString shouldEqual "AndExpr(IsTrueExpr(FieldReference(Filter1)),EqualExpr(FieldReference(Filter2),3))"
      }
    }
  }


  "Syntax.combinatoryConditionParentheses" should "be valid" in {
    val parseResult = new Syntax("[Filter1]||([Filter2]&&([Filter3]<3||[Filter4]>0))").QueryRule.run()
    parseResult shouldBe a [Success[_]]
    parseResult match {
      case Success(ast) => {
        ast.toString shouldEqual "OrExpr(IsTrueExpr(FieldReference(Filter1)),AndExpr(IsTrueExpr(FieldReference(Filter2))," +
          "OrExpr(LessThanExpr(FieldReference(Filter3),3),GreaterThanExpr(FieldReference(Filter4),0))))"
      }
    }
  }


  "Syntax.combinatoryConditionParenthesesComplex" should "be valid" in {
    val parseResult = new Syntax("[Filter1]&&(([Filter2]<3||[Filter2]>0)%%([Filter4]&&[Filter5]))").QueryRule.run()
    parseResult shouldBe a [Success[_]]
    parseResult match {
      case Success(ast) => {
        ast.toString shouldEqual "AndExpr(IsTrueExpr(FieldReference(Filter1)),XorExpr(OrExpr(LessThanExpr(FieldReference(Filter2),3)," +
          "GreaterThanExpr(FieldReference(Filter2),0)),AndExpr(IsTrueExpr(FieldReference(Filter4)),IsTrueExpr(FieldReference(Filter5)))))"
      }
    }
  }


  "Syntax.combinatoryConditionNonMatchingParentheses" should "be valid" in {
    val parseResult = new Syntax("[Filter1]&&([Filter2]<3||[Filter2]>0%%([Filter4])").QueryRule.run()
    parseResult shouldBe a [Failure[_]]
  }

  "Syntax.combinatoryConditionTypo" should "be valid" in {
    val parseResult = new Syntax("[Filter1]&[Filter2]<3)").QueryRule.run()
    parseResult shouldBe a [Failure[_]]
  }

  "Syntax.combinatoryConditionLeftToRightPriority" should "be valid" in {
    val parseResult = new Syntax("[Filter1]&&[Filter2]&&[Filter3]").QueryRule.run()
    parseResult shouldBe a [Success[_]]
    parseResult match {
      case Success(ast) => {
        ast.toString shouldEqual "AndExpr(AndExpr(IsTrueExpr(FieldReference(Filter1))," +
          "IsTrueExpr(FieldReference(Filter2))),IsTrueExpr(FieldReference(Filter3)))"
      }
    }
  }

  "Syntax.combinatoryConditionOperatorPriorities" should "be valid" in {
    val parseResult = new Syntax("[Filter1]||[Filter2]%%![Filter3]&&[Filter4]").QueryRule.run()
    parseResult shouldBe a [Success[_]]
    parseResult match {
      case Success(ast) => {
        ast.toString shouldEqual "OrExpr(IsTrueExpr(FieldReference(Filter1)),AndExpr(XorExpr(" +
          "IsTrueExpr(FieldReference(Filter2)),NotExpr(IsTrueExpr(FieldReference(Filter3)))),IsTrueExpr(FieldReference(Filter4))))"
      }
    }
  }


  "Syntax.combinatoryConditionOperatorPrioritiesParentheses" should "be valid" in {
    val parseResult = new Syntax("([Filter1]||[Filter2])&&![Filter3]%%!([Filter4]&&[Filter5])").QueryRule.run()
    parseResult shouldBe a [Success[_]]
    parseResult match {
      case Success(ast) => {
        ast.toString shouldEqual "AndExpr(OrExpr(IsTrueExpr(FieldReference(Filter1)),IsTrueExpr(FieldReference(Filter2)))," +
          "XorExpr(NotExpr(IsTrueExpr(FieldReference(Filter3))),NotExpr(AndExpr(IsTrueExpr(FieldReference(Filter4))," +
          "IsTrueExpr(FieldReference(Filter5))))))"
      }
    }
  }


  "Syntax.notConditionSimple" should "be valid" in {
    val parseResult = new Syntax("![Filter1]&&!([Filter2])").QueryRule.run()
    parseResult shouldBe a [Success[_]]
    parseResult match {
      case Success(ast) => {
        ast.toString shouldEqual "AndExpr(NotExpr(IsTrueExpr(FieldReference(Filter1)))," +
          "NotExpr(IsTrueExpr(FieldReference(Filter2))))"
      }
    }
  }

  "Syntax.notConditionSimpleParentheses" should "be valid" in {
    val parseResult = new Syntax("!([Filter1]&&![Filter2])").QueryRule.run()
    parseResult shouldBe a [Success[_]]
    parseResult match {
      case Success(ast) => {
        ast.toString shouldEqual "NotExpr(AndExpr(IsTrueExpr(FieldReference(Filter1))," +
          "NotExpr(IsTrueExpr(FieldReference(Filter2)))))"
      }
    }
  }

  "Syntax.notConditionComplex" should "be valid" in {
    val parseResult = new Syntax("!!([Filter1])&&!([Filter2]<=0||!([Filter3]&&![Filter4]%abc))").QueryRule.run()
    parseResult shouldBe a [Success[_]]
    parseResult match {
      case Success(ast) => {
        ast.toString shouldEqual "AndExpr(NotExpr(NotExpr(IsTrueExpr(FieldReference(Filter1))))," +
          "NotExpr(OrExpr(LessOrEqualExpr(FieldReference(Filter2),0),NotExpr(AndExpr(IsTrueExpr(FieldReference(Filter3))," +
          "NotExpr(LikeExpr(FieldReference(Filter4),abc)))))))"
      }
    }
  }
}