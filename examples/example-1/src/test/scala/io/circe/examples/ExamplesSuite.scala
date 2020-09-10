package io.circe.examples

import cats.instances.list._
import io.circe.testing.instances._
import io.circe.testing.golden._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.prop.Configuration
import org.typelevel.discipline.scalatest.FlatSpecDiscipline

class ExamplesSuite extends AnyFlatSpec with FlatSpecDiscipline with Configuration {
  checkAll("GoldenCodec[Foo]", GoldenCodecTests[Foo](10).goldenCodec)
  checkAll("GoldenCodec[Wub]", GoldenCodecTests[Wub].goldenCodec)
  // Validate that golden filenames don't collide for type constructors applied
  // to different arguments.
  checkAll("GoldenCodec[MyList[Foo]]", GoldenCodecTests[MyList[Foo]].goldenCodec)
  checkAll("GoldenCodec[MyList[Wub]]", GoldenCodecTests[MyList[Wub]].goldenCodec)

  // Validated that deeply nested types are successfully given golden filenames.
  checkAll(
    "GoldenCodec[MyTuple[MyList[Wub], MyList[MyList[Foo]]]]",
    GoldenCodecTests[MyTuple[MyList[Wub], MyList[MyList[Foo]]]].goldenCodec
  )

  checkAll("GoldenCodec[MyInner]", GoldenCodecTests[WithInnerClass.MyInner].goldenCodec)
}
