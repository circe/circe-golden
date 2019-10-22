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
