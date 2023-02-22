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

package io.circe.testing.golden.example

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

import java.time.Instant

case class Visit(id: Long, page: String, ts: Instant)

object Visit {
  implicit val codecForVisit: Codec[Visit] = deriveCodec
}

import cats.kernel.Eq
import io.circe.testing.{ ArbitraryInstances, CodecTests }
import org.scalacheck.Arbitrary
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.prop.Configuration
import org.typelevel.discipline.scalatest.FlatSpecDiscipline

trait VisitTestInstances extends ArbitraryInstances {
  implicit val eqVisit: Eq[Visit] = Eq.fromUniversalEquals
  implicit val arbitraryVisit: Arbitrary[Visit] = Arbitrary(
    for {
      id <- Arbitrary.arbitrary[Long]
      page <- Arbitrary.arbitrary[String]
      ts <- Arbitrary.arbitrary[Long].map(Instant.ofEpochMilli)
    } yield Visit(id, page, ts)
  )
}

class OldVisitSuite extends AnyFlatSpec with FlatSpecDiscipline with Configuration with VisitTestInstances {
  checkAll("Codec[Visit]", CodecTests[Visit].codec)

  val good = """{"id":12345,"page":"/index.html","ts":"2019-10-22T14:54:13Z"}"""
  val value = Visit(12345L, "/index.html", Instant.parse("2019-10-22T14:54:13Z"))

  "codecForVisit" should "decode JSON that's known to be good" in {
    assert(io.circe.jawn.decode[Visit](good) === Right(value))
  }

  it should "produce the expected results" in {
    import io.circe.syntax._
    assert(value.asJson.noSpaces === good)
  }
}

import io.circe.testing.golden.GoldenCodecTests

class VisitSuite extends AnyFlatSpec with FlatSpecDiscipline with Configuration with VisitTestInstances {
  checkAll("GoldenCodec[Visit]", GoldenCodecTests[Visit].goldenCodec)
}
