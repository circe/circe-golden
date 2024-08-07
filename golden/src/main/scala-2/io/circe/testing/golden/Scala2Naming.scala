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

import scala.reflect.runtime.universe.Symbol
import scala.reflect.runtime.universe.Type
import scala.reflect.runtime.universe.TypeTag

object Scala2Naming {

  /**
   * Attempt to guess the packaging of the type indicated by the provided type tag.
   */
  def inferPackage[A](implicit A: TypeTag[A]): List[String] =
    owners(A.tpe).collectFirst { case s if s.isPackage => s.fullName.split('.').toList }.getOrElse(List.empty)

  private def owners(tpe: Type): Iterator[Symbol] =
    Iterator.iterate(tpe.typeSymbol)(_.owner)

  /**
   * Attempt to guess the name of the type indicated by the provided type tag.
   */
  def inferName[A](implicit A: TypeTag[A]): String = inferNameForType(A.tpe)

  private def baseSymbols(tpe: Type): List[Symbol] =
    owners(tpe).takeWhile(!_.isPackage).toList.reverse

  private def inferNameForType(tpe: Type): String = {
    val baseNames = baseSymbols(tpe).map(_.name.decodedName.toString)

    (baseNames ::: tpe.typeArgs.map(inferNameForType)).mkString("_")
  }
}
