package de.upb.cs.swt.delphi.querylanguage

import org.scalatest.{FlatSpec, Matchers}
import scala.util.{Failure, Success}

/**
  * Tests for the DelphiQL syntax.
  *
  * @author Lisa Nguyen Quang Do
  */
class SyntaxTest extends FlatSpec with Matchers {

  "Syntax.singularConditionWithOperator" should "be valid" in {
    val parseResult = new Syntax("Filter1=abc").QueryRule.run()
    parseResult shouldBe a [Success[_]]
    parseResult match {
      case Success(ast) => {
       ast.toString shouldEqual "EqualExpr(Filter1,abc)"
      }
    }
  }

  "Syntax.singularConditionNoOperator" should "be valid" in {
    val parseResult = new Syntax("Filter1").QueryRule.run()
    parseResult shouldBe a [Success[_]]
    parseResult match {
      case Success(ast) => {
        ast.toString shouldEqual "TrueExpr(Filter1)"
      }
    }
  }

  "Syntax.singularConditionTypo" should "be valid" in {
    val parseResult = new Syntax("Filter1=<3").QueryRule.run()
    parseResult shouldBe a [Failure[_]]
  }

  "Syntax.singularConditionOddCharacters" should "be valid" in {
    val parseResult = new Syntax("Filter1%Filter2%'}.:").QueryRule.run()
    parseResult shouldBe a [Failure[_]]
  }

  "Syntax.combinatoryConditionSimple" should "be valid" in {
    val parseResult = new Syntax("Filter1&&Filter2=3").QueryRule.run()
    parseResult shouldBe a [Success[_]]
    parseResult match {
      case Success(ast) => {
        ast.toString shouldEqual "AndExpr(TrueExpr(Filter1),EqualExpr(Filter2,3))"
      }
    }
  }

  "Syntax.combinatoryConditionParentheses" should "be valid" in {
    val parseResult = new Syntax("Filter1||(Filter2&&(Filter3<3||Filter4>0))").QueryRule.run()
    parseResult shouldBe a [Success[_]]
    parseResult match {
      case Success(ast) => {
        ast.toString shouldEqual "OrExpr(TrueExpr(Filter1),AndExpr(TrueExpr(Filter2)," +
          "OrExpr(LessThanExpr(Filter3,3),GreaterThanExpr(Filter4,0))))"
      }
    }
  }

  "Syntax.combinatoryConditionParenthesesComplex" should "be valid" in {
    val parseResult = new Syntax("Filter1&&((Filter2<3||Filter2>0)%%(Filter4&&Filter5))").QueryRule.run()
    parseResult shouldBe a [Success[_]]
    parseResult match {
      case Success(ast) => {
        ast.toString shouldEqual "AndExpr(TrueExpr(Filter1),XorExpr(OrExpr(LessThanExpr(Filter2,3)," +
          "GreaterThanExpr(Filter2,0)),AndExpr(TrueExpr(Filter4),TrueExpr(Filter5))))"
      }
    }
  }

  "Syntax.combinatoryConditionNonMatchingParentheses" should "be valid" in {
    val parseResult = new Syntax("Filter1&&(Filter2<3||Filter2>0%%(Filter4)").QueryRule.run()
    parseResult shouldBe a [Failure[_]]
  }

  "Syntax.combinatoryConditionTypo" should "be valid" in {
    val parseResult = new Syntax("Filter1&Filter2<3)").QueryRule.run()
    parseResult shouldBe a [Failure[_]]
  }

  "Syntax.combinatoryConditionLeftToRightPriority" should "be valid" in {
    val parseResult = new Syntax("Filter1&&Filter2&&Filter3").QueryRule.run()
    parseResult shouldBe a [Success[_]]
    parseResult match {
      case Success(ast) => {
        ast.toString shouldEqual "AndExpr(AndExpr(TrueExpr(Filter1)," +
          "TrueExpr(Filter2)),TrueExpr(Filter3))"
      }
    }
  }

  "Syntax.combinatoryConditionOperatorPriorities" should "be valid" in {
    val parseResult = new Syntax("Filter1||Filter2%%!Filter3&&Filter4").QueryRule.run()
    parseResult shouldBe a [Success[_]]
    parseResult match {
      case Success(ast) => {
        ast.toString shouldEqual "OrExpr(TrueExpr(Filter1),AndExpr(XorExpr(" +
          "TrueExpr(Filter2),NotExpr(TrueExpr(Filter3))),TrueExpr(Filter4)))"
      }
    }
  }

  "Syntax.combinatoryConditionOperatorPrioritiesParentheses" should "be valid" in {
    val parseResult = new Syntax("(Filter1||Filter2)&&!Filter3%%!(Filter4&&Filter5)").QueryRule.run()
    parseResult shouldBe a [Success[_]]
    parseResult match {
      case Success(ast) => {
        ast.toString shouldEqual "AndExpr(OrExpr(TrueExpr(Filter1),TrueExpr(Filter2))," +
          "XorExpr(NotExpr(TrueExpr(Filter3)),NotExpr(AndExpr(TrueExpr(Filter4)," +
          "TrueExpr(Filter5)))))"
      }
    }
  }

  "Syntax.notConditionSimple" should "be valid" in {
    val parseResult = new Syntax("!Filter1&&!(Filter2)").QueryRule.run()
    parseResult shouldBe a [Success[_]]
    parseResult match {
      case Success(ast) => {
        ast.toString shouldEqual "AndExpr(NotExpr(TrueExpr(Filter1))," +
          "NotExpr(TrueExpr(Filter2)))"
      }
    }
  }

  "Syntax.notConditionSimpleParentheses" should "be valid" in {
    val parseResult = new Syntax("!(Filter1&&!Filter2)").QueryRule.run()
    parseResult shouldBe a [Success[_]]
    parseResult match {
      case Success(ast) => {
        ast.toString shouldEqual "NotExpr(AndExpr(TrueExpr(Filter1)," +
          "NotExpr(TrueExpr(Filter2))))"
      }
    }
  }

  "Syntax.notConditionComplex" should "be valid" in {
    val parseResult = new Syntax("!!(Filter1)&&!(Filter2<=0||!(Filter3&&!Filter4%abc))").QueryRule.run()
    parseResult shouldBe a [Success[_]]
    parseResult match {
      case Success(ast) => {
        ast.toString shouldEqual "AndExpr(NotExpr(NotExpr(TrueExpr(Filter1)))," +
          "NotExpr(OrExpr(LessOrEqualExpr(Filter2,0),NotExpr(AndExpr(TrueExpr(Filter3)," +
          "NotExpr(LikeExpr(Filter4,abc)))))))"
      }
    }
  }
}