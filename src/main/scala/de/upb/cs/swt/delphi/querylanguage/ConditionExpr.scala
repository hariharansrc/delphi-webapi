package de.upb.cs.swt.delphi.querylanguage

trait ConditionExpr extends Expression {

  def -->(expr: Expression): Expression = {
    println("Evaluating: " + expr)
    expr match {
      case AndExpr(left, right) => AndExpr(left, right)
      case OrExpr(left, right) => OrExpr(left, right)
      case NotExpr(expr) => -->(expr)
      case XorExpr(left, right) => XorExpr(left, right)
      case _ => null
    }
  }
}

case class AndExpr(Left: Expression, Right: Expression) extends ConditionExpr
case class OrExpr(Left: Expression, Right: Expression) extends ConditionExpr
case class NotExpr(Expr: Expression) extends ConditionExpr
case class XorExpr(Left: Expression, Right: Expression) extends ConditionExpr
