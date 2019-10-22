package io.circe.examples

import cats.kernel.Eq
import cats.kernel.instances.long._
import cats.syntax.functor._
import io.circe.{ Decoder, DecodingFailure, Encoder, Json }
import org.scalacheck.{ Arbitrary, Gen }

case class Wub(x: Long)

object Wub {
  implicit val eqWub: Eq[Wub] = Eq.by(_.x)
  implicit val arbitraryWub: Arbitrary[Wub] = Arbitrary(Arbitrary.arbitrary[Long].map(Wub(_)))

  implicit val decodeWub: Decoder[Wub] = Decoder[Long].prepare(_.downField("x")).map(Wub(_))
  implicit val encodeWub: Encoder[Wub] = Encoder.instance(w => Json.obj("x" -> Json.fromLong(w.x)))
}

sealed trait Foo
case class Bar(i: Int, s: String) extends Foo
case class Baz(xs: List[String]) extends Foo
case class Bam(w: Wub, d: Double) extends Foo

object Bar {
  implicit val eqBar: Eq[Bar] = Eq.fromUniversalEquals
  implicit val arbitraryBar: Arbitrary[Bar] = Arbitrary(
    for {
      i <- Arbitrary.arbitrary[Int]
      s <- Arbitrary.arbitrary[String]
    } yield Bar(i, s)
  )

  val decodeBar: Decoder[Bar] = Decoder.forProduct2("i", "s")(Bar.apply)
  val encodeBar: Encoder[Bar] = Encoder.forProduct2("i", "s") {
    case Bar(i, s) => (i, s)
  }
}

object Baz {
  implicit val eqBaz: Eq[Baz] = Eq.fromUniversalEquals
  implicit val arbitraryBaz: Arbitrary[Baz] = Arbitrary(
    Arbitrary.arbitrary[List[String]].map(Baz.apply)
  )

  implicit val decodeBaz: Decoder[Baz] = Decoder[List[String]].map(Baz(_))
  implicit val encodeBaz: Encoder[Baz] = Encoder.instance {
    case Baz(xs) => Json.fromValues(xs.map(Json.fromString))
  }
}

object Bam {
  implicit val eqBam: Eq[Bam] = Eq.fromUniversalEquals
  implicit val arbitraryBam: Arbitrary[Bam] = Arbitrary(
    for {
      w <- Arbitrary.arbitrary[Wub]
      d <- Arbitrary.arbitrary[Double]
    } yield Bam(w, d)
  )

  val decodeBam: Decoder[Bam] = Decoder.forProduct2("w", "d")(Bam.apply)(Wub.decodeWub, implicitly)
  val encodeBam: Encoder[Bam] = Encoder.forProduct2[Bam, Wub, Double]("w", "d") {
    case Bam(w, d) => (w, d)
  }(Wub.encodeWub, implicitly)
}

object Foo {
  implicit val eqFoo: Eq[Foo] = Eq.fromUniversalEquals

  implicit val arbitraryFoo: Arbitrary[Foo] = Arbitrary(
    Gen.oneOf(
      Arbitrary.arbitrary[Bar],
      Arbitrary.arbitrary[Baz],
      Arbitrary.arbitrary[Bam]
    )
  )

  implicit val encodeFoo: Encoder[Foo] = Encoder.instance {
    case bar @ Bar(_, _) => Json.obj("Bar" -> Bar.encodeBar(bar))
    case baz @ Baz(_)    => Json.obj("Baz" -> Baz.encodeBaz(baz))
    case bam @ Bam(_, _) => Json.obj("Bam" -> Bam.encodeBam(bam))
  }

  implicit val decodeFoo: Decoder[Foo] = Decoder.instance { c =>
    c.keys.map(_.toVector) match {
      case Some(Vector("Bar")) => c.get("Bar")(Bar.decodeBar.widen)
      case Some(Vector("Baz")) => c.get("Baz")(Baz.decodeBaz.widen)
      case Some(Vector("Bam")) => c.get("Bam")(Bam.decodeBam.widen)
      case _                   => Left(DecodingFailure("Foo", c.history))
    }
  }
}
