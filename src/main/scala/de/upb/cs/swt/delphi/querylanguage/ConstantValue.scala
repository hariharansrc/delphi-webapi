package de.upb.cs.swt.delphi.querylanguage

trait Expression {
}

class Constant(value: String) extends Expression {
  override def toString: String = value
}

case class ConstantValue(v : String) extends Constant(v) with ConditionExpr