package de.upb.cs.swt.delphi.querylanguage

trait Expression {}

trait ConditionExpr extends Expression {
  def -->(expr: Expression): Expression = {
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

trait SingularConditionExpr extends ConditionExpr {
  override def -->(expr: Expression): Expression = {
    expr match {
      case EqualExpr(left, right) => EqualExpr(left, right)
      case NotEqualExpr(left, right) => NotEqualExpr(left, right)
      case GreaterThanExpr(left, right) => GreaterThanExpr(left, right)
      case GreaterOrEqualExpr(left, right) => GreaterOrEqualExpr(left, right)
      case LessThanExpr(left, right) => LessThanExpr(left, right)
      case LessOrEqualExpr(left, right) => LessOrEqualExpr(left, right)
      case LikeExpr(left, right) => LikeExpr(left, right)
      case TrueExpr(expr) => TrueExpr(expr)
      case _ => null
    }
  }
}

case class EqualExpr(Left: String, Right: String) extends SingularConditionExpr
case class NotEqualExpr(Left: String, Right: String) extends SingularConditionExpr
case class GreaterThanExpr(Left: String, Right: String) extends SingularConditionExpr
case class GreaterOrEqualExpr(Left: String, Right: String) extends SingularConditionExpr
case class LessThanExpr(Left: String, Right: String) extends SingularConditionExpr
case class LessOrEqualExpr(Left: String, Right: String) extends SingularConditionExpr
case class LikeExpr(Left: String, Right: String) extends SingularConditionExpr
case class TrueExpr(Expr: String) extends SingularConditionExpr
