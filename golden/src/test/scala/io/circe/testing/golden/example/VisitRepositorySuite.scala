package io.circe.testing.golden.example

import io.circe.{ Codec, Printer }
import io.circe.generic.semiauto.deriveCodec
import java.time.Instant

case class VisitRepository(visits: Map[String, Visit])

object VisitRepository {
  implicit val codecForVisitRepository: Codec[VisitRepository] = deriveCodec
}

import cats.kernel.Eq
import io.circe.testing.{ ArbitraryInstances, CodecTests }
import org.scalacheck.Arbitrary
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.prop.Configuration
import org.typelevel.discipline.scalatest.FlatSpecDiscipline

trait VisitRepositoryTestInstances extends VisitTestInstances with ArbitraryInstances {
  implicit val eqVisitRepository: Eq[VisitRepository] = Eq.fromUniversalEquals
  implicit val arbitraryVisitRepository: Arbitrary[VisitRepository] = Arbitrary(
    for {
      visits <- Arbitrary.arbitrary[Map[String, Visit]]
    } yield VisitRepository(visits)
  )
}

class OldVisitRepositorySuite
    extends AnyFlatSpec
    with FlatSpecDiscipline
    with Configuration
    with VisitRepositoryTestInstances {
  checkAll("Codec[VisitRepository]", CodecTests[VisitRepository].codec)

  val good = """{"visits":{"1":{"id":12345,"page":"/index.html","ts":"2019-10-22T14:54:13Z"}}}"""
  val value = VisitRepository(Map("1" -> Visit(12345L, "/index.html", Instant.parse("2019-10-22T14:54:13Z"))))

  "codecForVisitRepository" should "decode JSON that's known to be good" in {
    assert(io.circe.jawn.decode[VisitRepository](good) === Right(value))
  }

  it should "produce the expected results" in {
    import io.circe.syntax._
    assert(value.asJson.noSpaces === good)
  }
}

import io.circe.testing.golden.GoldenCodecTests

class VisitRepositorySuite
    extends AnyFlatSpec
    with FlatSpecDiscipline
    with Configuration
    with VisitRepositoryTestInstances {
  checkAll("GoldenCodec[VisitRepository]", GoldenCodecTests[VisitRepository](Printer.spaces2SortKeys).goldenCodec)
}
