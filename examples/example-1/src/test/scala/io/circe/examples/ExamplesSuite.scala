package io.circe.examples

import io.circe.testing.instances._
import io.circe.testing.golden._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.prop.Configuration
import org.typelevel.discipline.scalatest.FlatSpecDiscipline

class ExamplesSuite extends AnyFlatSpec with FlatSpecDiscipline with Configuration {
  checkAll("GoldenCodec[Foo]", GoldenCodecTests[Foo](10).goldenCodec)
  checkAll("GoldenCodec[Wub]", GoldenCodecTests[Wub].goldenCodec)
}
