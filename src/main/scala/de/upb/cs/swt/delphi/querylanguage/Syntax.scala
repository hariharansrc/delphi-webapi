package de.upb.cs.swt.delphi.querylanguage

import org.parboiled2.{CharPredicate, Parser, ParserInput, Rule1}

/**
  * Created by benhermann on 03.02.18.
  */
class Syntax(val input : ParserInput) extends Parser {

  def QueryRule = rule {
    QueryElementRule ~ EOI
  }

  def QueryElementRule = rule {
    FeatureIdentifierRule ~  ConditionRule
  }

  def FeatureIdentifierRule = rule {
    capture(oneOrMore(CharPredicate.Alpha))
  }

  def ConditionRule : Rule1[ConditionExpr] = rule {
    SingularConditionRule | AndRule | OrRule | NotRule | XorRule
  }

  def SingularConditionRule = rule {
    EqualRule | NotEqualRule | GreaterThanRule | GreaterOrEqual | LessThan | LessOrEqual | Like | True
  }

  /* Combinatory rules */

  def AndRule = rule {
    ConditionRule ~ "&&" ~ ConditionRule ~> AndExpr
  }
  def OrRule = rule {
    ConditionRule ~ "||" ~ ConditionRule ~> OrExpr
  }
  def NotRule = rule {
    "!" ~ ConditionRule ~> NotExpr
  }
  def XorRule = rule {
    ConditionRule ~ "XX" ~ ConditionRule ~> XorExpr
  }
  def Literal = rule {
    NumberLiteral | StringLiteral
  }

  /* Literals */

  def StringLiteral = rule {
    capture(oneOrMore(CharPredicate.AlphaNum))
  }

  def NumberLiteral = rule {
    capture(oneOrMore(CharPredicate.Digit))
  }

  /* Value operators */

  def EqualRule = rule {
    "=" ~ Literal ~> ConstantValue
  }
  def NotEqualRule = rule {
    "!=" ~ NumberLiteral ~> ConstantValue
  }
  def GreaterThanRule = rule {
    ">" ~ NumberLiteral ~> ConstantValue
  }
  def GreaterOrEqual = rule {
    ">=" ~ NumberLiteral ~> ConstantValue
  }
  def LessThan = rule {
    "<" ~ NumberLiteral ~> ConstantValue
  }
  def LessOrEqual = rule {
    "<=" ~ NumberLiteral ~> ConstantValue
  }
  def Like = rule {
    "%" ~ StringLiteral ~ "%" ~> ConstantValue
  }
  def True = rule {
    StringLiteral ~> ConstantValue
  }
}

class StringLiteral(value : String) {}

class NumberLiteral(value : Integer) {}
