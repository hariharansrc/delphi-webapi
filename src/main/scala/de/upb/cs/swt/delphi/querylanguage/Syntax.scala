package de.upb.cs.swt.delphi.querylanguage

import org.parboiled2.{CharPredicate, Parser, ParserInput, Rule1}

/**
  * Created by benhermann on 03.02.18.
  */
class Syntax(val input : ParserInput) extends Parser {

  def QueryRule = rule {
    ConditionRule ~ EOI
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

  /* Literals */

  def Literal = rule {
    NumberLiteral | StringLiteral
  }

  def StringLiteral = rule {
    capture(oneOrMore(CharPredicate.AlphaNum))
  }

  def NumberLiteral = rule {
    capture(oneOrMore(CharPredicate.Digit))
  }

  /* Value operators */

  def EqualRule = rule {
    StringLiteral ~ "=" ~ Literal ~> EqualExpr
  }
  def NotEqualRule = rule {
    StringLiteral ~ "!=" ~ Literal ~> NotEqualExpr
  }
  def GreaterThanRule = rule {
    StringLiteral ~ ">" ~ NumberLiteral ~> GreaterThanExpr
  }
  def GreaterOrEqual = rule {
    StringLiteral ~ ">=" ~ NumberLiteral ~> GreaterOrEqualExpr
  }
  def LessThan = rule {
    StringLiteral ~ "<" ~ NumberLiteral ~> LessThanExpr
  }
  def LessOrEqual = rule {
    StringLiteral ~ "<=" ~ NumberLiteral ~> LessOrEqualExpr
  }
  def Like = rule {
    StringLiteral ~ "%" ~ StringLiteral ~ "%" ~> LikeExpr
  }
  def True = rule {
    StringLiteral ~> TrueExpr
  }
}


