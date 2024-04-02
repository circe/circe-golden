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

import org.scalacheck.Gen
import org.scalacheck.rng.Seed

import scala.util.Try

trait ExampleGeneration[A] { self: GoldenCodecLaws[A] =>
  def size: Int
  def gen: Gen[A]

  protected lazy val params: Gen.Parameters = Gen.Parameters.default.withSize(size)

  final def getValue(seed: Seed): A = gen.pureApply(params, seed)
  final def getValueFromBase64Seed(seed: String): Try[A] = Seed.fromBase64(seed).map(getValue)

  final def generateRandomGoldenExamples(count: Int): List[(Seed, A, String)] =
    (0 until count).map { _ =>
      val seed = Seed.random()
      val value = getValue(seed)

      (seed, value, printJson(encode(value)))
    }.toList
}
