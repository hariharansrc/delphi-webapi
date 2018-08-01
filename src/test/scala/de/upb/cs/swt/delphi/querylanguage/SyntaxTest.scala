package de.upb.cs.swt.delphi.querylanguage

import org.scalatest.{FlatSpec, Matchers}
import scala.util.{Failure, Success}

class SyntaxTest extends FlatSpec with Matchers {

  "Syntax.oneFilterEqual" should "be valid" in {
    val parseResult = new Syntax("Filter1=3").QueryRule.run()
    parseResult shouldBe a [Success[_]]
  }

  "Syntax.oneFilterTrue" should "be valid" in {
    val parseResult = new Syntax("Filter1").QueryRule.run()
    parseResult shouldBe a [Success[_]]
  }

  "Syntax.oneFilterLessOrEqualTypo" should "be valid" in {
    val parseResult = new Syntax("Filter1=<3").QueryRule.run()
    parseResult shouldBe a [Failure[_]]
  }

  // TODO: Fix infinite loop.
  "Syntax.oneFilterFalse" should "be valid" in {
    val parseResult = new Syntax("!Filter2").QueryRule.run()
    parseResult shouldBe a [Success[_]]
  }

  // TODO: Fix false test. Related to the infinite loop issue.
  "Syntax.twoFiltersOr" should "be valid" in {
    val parseResult = new Syntax("Filter1=3||Filter2=5").QueryRule.run()
    parseResult shouldBe a [Success[_]]
  }

}