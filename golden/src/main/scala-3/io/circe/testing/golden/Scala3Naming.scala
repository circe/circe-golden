/*
 * Copyright 2016 circe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.circe.testing.golden

import scala.quoted.*
object Scala3Naming {

  inline def name[T]: String = ${ Scala3Naming.nameImpl[T] }
  inline def pkg[T]: List[String] = ${ Scala3Naming.packageImpl[T] }

  def nameImpl[T: Type](using Quotes): Expr[String] = {
    import quotes.reflect.*

    val typeRepr = TypeRepr.of[T]

    def expandName(repr: TypeRepr, names: List[String]): List[String] = {
      val args = repr.typeArgs
      repr.typeSymbol.name :: (if args.isEmpty then names else args.flatMap(x => expandName(x, names)))
    }

    Expr(expandName(typeRepr, Nil).reverse.mkString("_"))
  }

  def packageImpl[T: Type](using q: Quotes): Expr[List[String]] = {
    import q.reflect.*
    val repr = TypeRepr.of[T]
    val symbol = repr.typeSymbol

    def expandPkg(s: Symbol, names: List[String]): List[String] =
      if s.isNoSymbol || s.name == "<root>" then names
      else expandPkg(s.maybeOwner, if s.isPackageDef then s.name :: names else names)
    Expr(expandPkg(symbol, Nil))
  }
}
