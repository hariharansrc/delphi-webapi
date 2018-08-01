package de.upb.cs.swt.delphi.querylanguage

trait CombinatorialExpr {
  def -->(expr: CombinatorialExpr): CombinatorialExpr = {
    expr match {
      case AndExpr(left, right) => AndExpr(left, right)
      case OrExpr(left, right) => OrExpr(left, right)
      case NotExpr(expr) => -->(expr)
      case XorExpr(left, right) => XorExpr(left, right)
      case _ => null
    }
  }
}

case class AndExpr(Left: CombinatorialExpr, Right: CombinatorialExpr) extends CombinatorialExpr
case class OrExpr(Left: CombinatorialExpr, Right: CombinatorialExpr) extends CombinatorialExpr
case class NotExpr(Expr: CombinatorialExpr) extends CombinatorialExpr
case class XorExpr(Left: CombinatorialExpr, Right: CombinatorialExpr) extends CombinatorialExpr

trait SingularConditionExpr extends CombinatorialExpr {
  override def -->(expr: CombinatorialExpr): CombinatorialExpr = {
    expr match {
      case EqualExpr(left, right) => EqualExpr(left, right)
      case NotEqualExpr(left, right) => NotEqualExpr(left, right)
      case GreaterThanExpr(left, right) => GreaterThanExpr(left, right)
      case GreaterOrEqualExpr(left, right) => GreaterOrEqualExpr(left, right)
      case LessThanExpr(left, right) => LessThanExpr(left, right)
      case LessOrEqualExpr(left, right) => LessOrEqualExpr(left, right)
      case LikeExpr(left, right) => LikeExpr(left, right)
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
