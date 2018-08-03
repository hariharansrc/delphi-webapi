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
  }

  "Syntax.singularConditionNoOperator" should "be valid" in {
    val parseResult = new Syntax("Filter1").QueryRule.run()
    parseResult shouldBe a [Success[_]]
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
  }

  "Syntax.combinatoryConditionParentheses" should "be valid" in {
    val parseResult = new Syntax("Filter1||Filter2&&(Filter3<3||Filter4>0)").QueryRule.run()
    parseResult shouldBe a [Success[_]]
  }

  "Syntax.combinatoryConditionParenthesesComplex" should "be valid" in {
    val parseResult = new Syntax("Filter1&&(Filter2<3||Filter2>0%%(Filter4&&Filter5))").QueryRule.run()
    parseResult shouldBe a [Success[_]]
  }

  "Syntax.combinatoryConditionNonMatchingParentheses" should "be valid" in {
    val parseResult = new Syntax("Filter1&&(Filter2<3||Filter2>0%%(Filter4)").QueryRule.run()
    parseResult shouldBe a [Failure[_]]
  }

  "Syntax.combinatoryConditionTypo" should "be valid" in {
    val parseResult = new Syntax("Filter1&Filter2<3)").QueryRule.run()
    parseResult shouldBe a [Failure[_]]
  }

  "Syntax.notConditionSimple" should "be valid" in {
    val parseResult = new Syntax("Filter1&&!Filter2").QueryRule.run()
    parseResult shouldBe a [Success[_]]
  }

  "Syntax.notConditionSimpleParentheses" should "be valid" in {
    val parseResult = new Syntax("!(Filter1&&Filter2)").QueryRule.run()
    parseResult shouldBe a [Success[_]]
  }

  "Syntax.notConditionComplex" should "be valid" in {
    val parseResult = new Syntax("!!(Filter1)&&!(Filter2<=0||!(Filter3&&!Filter4%abc))").QueryRule.run()
    parseResult shouldBe a [Success[_]]
  }

}