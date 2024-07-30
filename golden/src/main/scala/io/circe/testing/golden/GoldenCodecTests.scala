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

import cats.instances.string._
import cats.kernel.Eq
import cats.laws.IsEq
import cats.laws.discipline.catsLawsIsEqToProp
import io.circe.Json
import io.circe.testing.CodecTests
import org.scalacheck.Arbitrary
import org.scalacheck.Prop
import org.scalacheck.Shrink

import scala.util.Failure
import scala.util.Success
import scala.util.Try

trait GoldenCodecTests[A] extends CodecTests[A] {
  def laws: GoldenCodecLaws[A]

  private[this] def tryListToProp[T: Eq](result: Try[List[IsEq[T]]]): Prop = result match {
    case Failure(error)      => Prop.exception(error)
    case Success(equalities) => Prop.all(equalities.map(catsLawsIsEqToProp(_)): _*)
  }

  def goldenCodec(implicit
    arbitraryA: Arbitrary[A],
    shrinkA: Shrink[A],
    eqA: Eq[A],
    arbitraryJson: Arbitrary[Json],
    shrinkJson: Shrink[Json]
  ): RuleSet = new DefaultRuleSet(
    name = "goldenCodec",
    parent = Some(codec),
    "decoding golden files" -> tryListToProp(laws.goldenDecoding),
    "encoding golden files" -> tryListToProp(laws.goldenEncoding)
  )

  def unserializableGoldenCodec(implicit
    arbitraryA: Arbitrary[A],
    shrinkA: Shrink[A],
    eqA: Eq[A],
    arbitraryJson: Arbitrary[Json],
    shrinkJson: Shrink[Json]
  ): RuleSet = new DefaultRuleSet(
    name = "goldenCodec",
    parent = Some(unserializableCodec),
    "decoding golden files" -> tryListToProp(laws.goldenDecoding),
    "encoding golden files" -> tryListToProp(laws.goldenEncoding)
  )
}

object GoldenCodecTests extends GoldenCodecTestsCompanion {

  // for binary compat
  protected def apply[A](laws0: GoldenCodecLaws[A]): GoldenCodecTests[A] = fromLaws(laws0)
  def fromLaws[A](laws0: GoldenCodecLaws[A]): GoldenCodecTests[A] =
    new GoldenCodecTests[A] {
      val laws: GoldenCodecLaws[A] = laws0
    }
}
