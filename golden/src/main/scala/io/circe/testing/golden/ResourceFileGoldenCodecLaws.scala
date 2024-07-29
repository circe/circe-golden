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

import cats.instances.list._
import cats.instances.try_._
import cats.syntax.apply._
import cats.syntax.traverse._
import io.circe.Decoder
import io.circe.Encoder
import io.circe.Printer
import org.scalacheck.Arbitrary
import org.scalacheck.Gen

import java.io.File
import java.io.PrintWriter
import scala.util.Failure
import scala.util.Try
import scala.util.matching.Regex

abstract class ResourceFileGoldenCodecLaws[A](
  name: String,
  resourceRootDir: File,
  resourcePackage: List[String],
  val size: Int,
  count: Int,
  override protected val printer: Printer
) extends GoldenCodecLaws[A]
    with ExampleGeneration[A] {

  private[this] val resourceRootPath: String = "/" + resourcePackage.mkString("/") + "/"
  private[this] val resourceDir: File = resourcePackage.foldLeft(resourceRootDir) { case (acc, p) =>
    new File(acc, p)
  }
  private[this] val GoldenFilePattern: Regex = "^-(.{44})\\.json$".r

  private[this] lazy val loadGoldenFiles: Try[List[(A, String)]] =
    Resources.open(resourceRootPath).flatMap { dirSource =>
      val files = dirSource
        .getLines()
        .flatMap {
          case fileName if fileName.startsWith(name) =>
            fileName.drop(name.length) match {
              case GoldenFilePattern(seed) => Some((seed, fileName))
              case _                       => None
            }
          case _ => None
        }
        .toList
        .traverse[Try, (A, String)] { case (seed, name) =>
          val contents = Resources.open(resourceRootPath + name).map { source =>
            val lines = source.getLines().mkString("\n")
            source.close()
            lines
          }
          (getValueFromBase64Seed(seed), contents).tupled
        }

      dirSource.close()

      // Fail if we don't have either zero golden files or the required number.
      files.flatMap { values =>
        if (values.size == 0 || values.size == count) files
        else Failure(new IllegalStateException(s"Expected 0 or $count golden files, got ${values.size}"))
      }
    }

  private[this] def generateGoldenFiles: Try[List[(A, String)]] =
    generateRandomGoldenExamples(count).traverse { case (seed, value, encoded) =>
      Try {
        resourceDir.mkdirs()
        val file = new File(resourceDir, s"$name-${seed.toBase64}.json")

        val writer = new PrintWriter(file)
        writer.print(encoded)
        writer.close()

        (value, encoded)
      }
    }

  protected lazy val goldenExamples: Try[List[(A, String)]] =
    loadGoldenFiles.flatMap(fs => if (fs.isEmpty) generateGoldenFiles else loadGoldenFiles)
}

object ResourceFileGoldenCodecLaws extends ResourceFileGoldenCodecLawsCompanion {
  def apply[A](
    name: String,
    resourceRootDir: File,
    resourcePackage: List[String],
    size: Int,
    count: Int,
    printer: Printer
  )(implicit decodeA: Decoder[A], encodeA: Encoder[A], arbitraryA: Arbitrary[A]): GoldenCodecLaws[A] =
    new ResourceFileGoldenCodecLaws[A](name, resourceRootDir, resourcePackage, size, count, printer) {
      val decode: Decoder[A] = decodeA
      val encode: Encoder[A] = encodeA
      val gen: Gen[A] = arbitraryA.arbitrary
    }
}
