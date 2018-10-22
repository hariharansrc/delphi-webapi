// Copyright (C) 2018 The Delphi Team.
// See the LICENCE file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
