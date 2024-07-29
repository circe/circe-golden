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

package io.circe.testing.golden

import io.circe.Decoder
import io.circe.Encoder
import io.circe.Printer
import org.scalacheck.Arbitrary

import scala.reflect.ClassTag

trait ResourceFileGoldenCodecLawsCompanion { self: ResourceFileGoldenCodecLaws.type =>

  inline def apply[A](
    size: Int = 100,
    count: Int = 1,
    printer: Printer = Printer.spaces2
  )(using
    decodeA: Decoder[A],
    encodeA: Encoder[A],
    arbitraryA: Arbitrary[A],
    classT: ClassTag[A]
  ): GoldenCodecLaws[A] =
    self.apply[A](
      Scala3Naming.name[A],
      Resources.inferRootDir(classT.runtimeClass),
      Scala3Naming.pkg[A],
      size,
      count,
      printer
    )
}
