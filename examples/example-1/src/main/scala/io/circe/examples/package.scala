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

package io.circe.examples

import cats.kernel.Eq
import cats.kernel.instances.long._
import cats.syntax.functor._
import io.circe.Decoder
import io.circe.DecodingFailure
import io.circe.Encoder
import io.circe.Json
import org.scalacheck.Arbitrary
import org.scalacheck.Gen

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
  val encodeBar: Encoder[Bar] = Encoder.forProduct2("i", "s") { case Bar(i, s) =>
    (i, s)
  }
}

object Baz {
  implicit val eqBaz: Eq[Baz] = Eq.fromUniversalEquals
  implicit val arbitraryBaz: Arbitrary[Baz] = Arbitrary(
    Arbitrary.arbitrary[List[String]].map(Baz.apply)
  )

  implicit val decodeBaz: Decoder[Baz] = Decoder[List[String]].map(Baz(_))
  implicit val encodeBaz: Encoder[Baz] = Encoder.instance { case Baz(xs) =>
    Json.fromValues(xs.map(Json.fromString))
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
  val encodeBam: Encoder[Bam] = Encoder.forProduct2[Bam, Wub, Double]("w", "d") { case Bam(w, d) =>
    (w, d)
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

case class MyList[A](value: List[A])

object MyList {
  import cats.kernel.instances.list._

  implicit def eqMyList[A](implicit A: Eq[A]): Eq[MyList[A]] = Eq.by[MyList[A], List[A]](_.value)
  implicit def arbitraryMyList[A](implicit A: Arbitrary[A]): Arbitrary[MyList[A]] =
    Arbitrary(Arbitrary.arbitrary[List[A]].map(as => MyList(as.take(2))))

  implicit def encodeMyList[A](implicit A: Encoder[A]): Encoder[MyList[A]] =
    Encoder[List[A]].contramap(_.value)

  implicit def decodeMyList[A](implicit A: Decoder[A]): Decoder[MyList[A]] =
    Decoder[List[A]].map(MyList(_))
}

case class MyTuple[A, B](value: (A, B))

object MyTuple {
  import cats.kernel.instances.tuple._

  implicit def eqMyTuple[A, B](implicit A: Eq[A], B: Eq[B]): Eq[MyTuple[A, B]] = Eq.by[MyTuple[A, B], (A, B)](_.value)
  implicit def arbitraryMyTuple[A, B](implicit A: Arbitrary[A], B: Arbitrary[B]): Arbitrary[MyTuple[A, B]] =
    Arbitrary(Arbitrary.arbitrary[(A, B)].map(MyTuple(_)))

  implicit def encodeMyTuple[A, B](implicit A: Encoder[A], B: Encoder[B]): Encoder[MyTuple[A, B]] =
    Encoder[(A, B)].contramap(_.value)

  implicit def decodeMyTuple[A, B](implicit A: Decoder[A], B: Decoder[B]): Decoder[MyTuple[A, B]] =
    Decoder[(A, B)].map(MyTuple(_))
}

object WithInnerClass {
  case class MyInner(i: Int)

  object MyInner {
    implicit val eqMyInner: Eq[MyInner] = Eq.fromUniversalEquals
    implicit val arbitraryMyInner: Arbitrary[MyInner] = Arbitrary(Arbitrary.arbitrary[Int].map(MyInner.apply))

    implicit val encodeMyInner: Encoder[MyInner] = Encoder[Int].contramap(_.i)
    implicit val decodeMyInner: Decoder[MyInner] = Decoder[Int].map(MyInner.apply)
  }
}
