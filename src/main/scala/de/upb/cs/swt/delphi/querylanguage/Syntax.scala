package de.upb.cs.swt.delphi.querylanguage

import org.parboiled2.{CharPredicate, Parser, ParserInput, Rule1}

/**
  * Created by benhermann on 03.02.18.
  * Author: Lisa Nguyen Quang Do 01.08.2018
  */
class Syntax(val input : ParserInput) extends Parser {

  def QueryRule = rule {
    CombinatorialRule ~ EOI
  }

  // Combinatorial rules.
  def CombinatorialRule : Rule1[CombinatorialExpr] = rule {
    NotRule |
    Factor ~ zeroOrMore(
        "&&" ~ Factor ~> AndExpr |
          "||" ~ Factor ~> OrExpr |
          "%%" ~ Factor ~> XorExpr)
  }

  // Handling parentheses.
  def Factor : Rule1[CombinatorialExpr] = rule {
    Parentheses | SingularConditionRule | NotRule
  }
  def Parentheses = rule { '(' ~ CombinatorialRule ~ ')' }
  def NotRule = rule { '!' ~ (CombinatorialRule | Parentheses) ~> NotExpr }

  // Singular conditions.
  def SingularConditionRule = rule {
    EqualRule | NotEqualRule | GreaterThanRule | GreaterOrEqual |
      LessThan | LessOrEqual | Like | True
  }
  def EqualRule = rule { Literal ~ "=" ~ Literal ~> EqualExpr }
  def NotEqualRule = rule { Literal ~ "!=" ~ Literal ~> NotEqualExpr }
  def GreaterThanRule = rule { Literal ~ ">" ~ Literal ~> GreaterThanExpr }
  def GreaterOrEqual = rule { Literal ~ ">=" ~ Literal ~> GreaterOrEqualExpr }
  def LessThan = rule { Literal ~ "<" ~ Literal ~> LessThanExpr }
  def LessOrEqual = rule { Literal ~ "<=" ~ Literal ~> LessOrEqualExpr }
  def Like = rule { Literal ~ "%" ~ Literal ~> LikeExpr }
  def True = rule { Literal ~> TrueExpr }

  // Literals
  def Literal = rule { capture(oneOrMore(CharPredicate.AlphaNum)) ~> (_.toString) }
}


