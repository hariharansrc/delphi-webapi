package de.upb.cs.swt.delphi.querylanguage

import org.parboiled2.{CharPredicate, Parser, ParserInput, Rule1}

/**
  * The syntax definition and parser for the Delphi QL.
  *
  * @author Lisa Nguyen Quang Do
  * @author Ben Hermann
  *
  */
class Syntax(val input : ParserInput) extends Parser {

  def QueryRule = rule {
    CombinatorialRule ~ EOI
  }

  // Combinatorial rules.
  def CombinatorialRule : Rule1[CombinatorialExpr] = rule {
    OrOrElseRule | NotRule
  }
  def OrOrElseRule = rule {
    AndOrElseRule ~ zeroOrMore("||" ~ AndOrElseRule ~> OrExpr)
  }
  def AndOrElseRule = rule {
    XorOrElseRule ~ zeroOrMore("&&" ~ XorOrElseRule ~> AndExpr)
  }
  def XorOrElseRule = rule {
    Factor ~ zeroOrMore("%%" ~ Factor ~> XorExpr)
  }

  // Handling parentheses.
  def Factor : Rule1[CombinatorialExpr] = rule {
    Parentheses | SingularConditionRule | NotRule
  }
  def Parentheses = rule { '(' ~ CombinatorialRule ~ ')' }
  def NotRule : Rule1[CombinatorialExpr] = rule {
    '!' ~ (NotRule | SingularConditionRule | Parentheses) ~> NotExpr
  }

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


