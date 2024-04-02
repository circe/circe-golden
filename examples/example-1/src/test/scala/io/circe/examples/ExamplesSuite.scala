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

import io.circe.testing.golden._
import io.circe.testing.instances._
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
