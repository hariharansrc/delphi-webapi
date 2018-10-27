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

case class AndExpr(left: CombinatorialExpr, right: CombinatorialExpr) extends CombinatorialExpr
case class OrExpr(left: CombinatorialExpr, right: CombinatorialExpr) extends CombinatorialExpr
case class NotExpr(expr: CombinatorialExpr) extends CombinatorialExpr
case class XorExpr(left: CombinatorialExpr, right: CombinatorialExpr) extends CombinatorialExpr

trait SingularConditionExpr extends CombinatorialExpr

case class EqualExpr(left: FieldReference, right: String) extends SingularConditionExpr
case class NotEqualExpr(left: FieldReference, right: String) extends SingularConditionExpr
case class GreaterThanExpr(left: FieldReference, right: String) extends SingularConditionExpr
case class GreaterOrEqualExpr(left: FieldReference, right: String) extends SingularConditionExpr
case class LessThanExpr(left: FieldReference, right: String) extends SingularConditionExpr
case class LessOrEqualExpr(left: FieldReference, right: String) extends SingularConditionExpr
case class LikeExpr(left: FieldReference, right: String) extends SingularConditionExpr
case class IsTrueExpr(fieldName: FieldReference) extends SingularConditionExpr
case class FieldReference(fieldName: String) extends CombinatorialExpr