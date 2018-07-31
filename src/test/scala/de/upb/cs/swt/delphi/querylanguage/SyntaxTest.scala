package de.upb.cs.swt.delphi.querylanguage

import org.scalatest.{FlatSpec, Matchers}
import scala.util.{Failure, Success}

class SyntaxTest extends FlatSpec with Matchers {

  "Syntax.oneFilterLessOrEqual" should "be valid" in {
    val parseResult = new Syntax("Filter<=3").QueryRule.run()
    parseResult shouldBe a [Success[_]]
  }

  // TODO: Fix that.
  "Syntax.oneFilterLessOrEqualTypo" should "be valid" in {
    val parseResult = new Syntax("Filter=<3").QueryRule.run()
    parseResult shouldBe a [Failure[_]]
  }

}