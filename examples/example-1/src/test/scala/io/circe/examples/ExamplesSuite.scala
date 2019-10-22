package io.circe.examples

import io.circe.testing.instances._
import io.circe.testing.golden._
import org.scalatest.flatspec.AnyFlatSpec
import org.typelevel.discipline.scalatest.Discipline

class ExamplesSuite extends AnyFlatSpec with Discipline {
  checkAll("GoldenCodec[Foo]", GoldenCodecTests[Foo](10).goldenCodec)
  checkAll("GoldenCodec[Wub]", GoldenCodecTests[Wub].goldenCodec)
}
