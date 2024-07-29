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

trait GoldenCodecTestsCompanion { self: GoldenCodecTests.type =>
  inline def apply[A: Decoder: Encoder: Arbitrary: ClassTag]: GoldenCodecTests[A] =
    fromLaws[A](ResourceFileGoldenCodecLaws[A]())

  inline def apply[A: Decoder: Encoder: Arbitrary: ClassTag](printer: Printer): GoldenCodecTests[A] =
    fromLaws[A](ResourceFileGoldenCodecLaws[A](printer = printer))

  inline def apply[A: Decoder: Encoder: Arbitrary: ClassTag](count: Int): GoldenCodecTests[A] =
    fromLaws[A](ResourceFileGoldenCodecLaws[A](count = count))

  inline def apply[A: Decoder: Encoder: Arbitrary: ClassTag](count: Int, printer: Printer): GoldenCodecTests[A] =
    fromLaws[A](ResourceFileGoldenCodecLaws[A](count = count, printer = printer))
}
