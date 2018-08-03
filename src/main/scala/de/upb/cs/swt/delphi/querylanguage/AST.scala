package de.upb.cs.swt.delphi.querylanguage

trait CombinatorialExpr

case class AndExpr(Left: CombinatorialExpr, Right: CombinatorialExpr) extends CombinatorialExpr
case class OrExpr(Left: CombinatorialExpr, Right: CombinatorialExpr) extends CombinatorialExpr
case class NotExpr(Expr: CombinatorialExpr) extends CombinatorialExpr
case class XorExpr(Left: CombinatorialExpr, Right: CombinatorialExpr) extends CombinatorialExpr

trait SingularConditionExpr extends CombinatorialExpr

case class EqualExpr(Left: String, Right: String) extends SingularConditionExpr
case class NotEqualExpr(Left: String, Right: String) extends SingularConditionExpr
case class GreaterThanExpr(Left: String, Right: String) extends SingularConditionExpr
case class GreaterOrEqualExpr(Left: String, Right: String) extends SingularConditionExpr
case class LessThanExpr(Left: String, Right: String) extends SingularConditionExpr
case class LessOrEqualExpr(Left: String, Right: String) extends SingularConditionExpr
case class LikeExpr(Left: String, Right: String) extends SingularConditionExpr
case class TrueExpr(Expr: String) extends SingularConditionExpr
