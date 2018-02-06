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

  def ConditionRule = rule {
    SingularConditionRule // | ConjunctionRule | DisjunctionRule
  }

  def SingularConditionRule = rule {
    EqualRule |
    NotEqualRule |
    GreaterThanRule |
    GreaterOrEqual |
    LessThan |
    LessOrEqual |
    Like
  }

  /*
  def ConjunctionRule = rule {
    ConditionRule ~ "&&" ~ ConditionRule
  }

  def DisjunctionRule = rule {
    ConditionRule ~ "||" ~ ConditionRule
  }
  */

  def Literal = rule {
    NumberLiteral |
    StringLiteral
  }

  def StringLiteral = rule {
    capture(oneOrMore(CharPredicate.AlphaNum))
  }

  def NumberLiteral = rule {
    capture(oneOrMore(CharPredicate.Digit))
  }

  def EqualRule = rule {
    "=" ~ Literal
  }
  def NotEqualRule = rule {
    "!=" ~ NumberLiteral
  }
  def GreaterThanRule = rule {
    ">" ~ NumberLiteral
  }
  def GreaterOrEqual = rule {
    ">=" ~ NumberLiteral
  }

  def LessThan = rule {
    "<" ~ NumberLiteral
  }
  def LessOrEqual = rule {
    "=<" ~ NumberLiteral
  }

  def Like = rule {
    "~" ~ StringLiteral
  }
}

class StringLiteral(value : String) {}

class NumberLiteral(value : Integer) {}
