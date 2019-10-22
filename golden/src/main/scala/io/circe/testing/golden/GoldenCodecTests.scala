package io.circe.testing.golden

import cats.instances.string._
import cats.kernel.Eq
import cats.laws.IsEq
import cats.laws.discipline.catsLawsIsEqToProp
import io.circe.{ Decoder, Encoder, Json }
import io.circe.testing.CodecTests
import org.scalacheck.{ Arbitrary, Prop, Shrink }
import scala.reflect.runtime.universe.TypeTag
import scala.util.{ Failure, Success, Try }

trait GoldenCodecTests[A] extends CodecTests[A] {
  def laws: GoldenCodecLaws[A]

  private[this] def tryListToProp[A: Eq](result: Try[List[IsEq[A]]]): Prop = result match {
    case Failure(error)      => Prop.exception(error)
    case Success(equalities) => Prop.all(equalities.map(catsLawsIsEqToProp(_)): _*)
  }

  def goldenCodec(
    implicit
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

  def unserializableGoldenCodec(
    implicit
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

object GoldenCodecTests {
  def apply[A: Decoder: Encoder: Arbitrary: TypeTag]: GoldenCodecTests[A] =
    apply[A](ResourceFileGoldenCodecLaws[A])

  def apply[A: Decoder: Encoder: Arbitrary: TypeTag](count: Int): GoldenCodecTests[A] =
    apply[A](ResourceFileGoldenCodecLaws[A](count))

  def apply[A: Decoder: Encoder: Arbitrary](laws0: GoldenCodecLaws[A]): GoldenCodecTests[A] =
    new GoldenCodecTests[A] {
      val laws: GoldenCodecLaws[A] = laws0
    }
}
