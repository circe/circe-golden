package io.circe.testing.golden

import cats.instances.list._, cats.instances.try_._
import cats.syntax.apply._, cats.syntax.traverse._
import io.circe.{ Decoder, Encoder, Printer }
import java.io.{ File, PrintWriter }
import org.scalacheck.{ Arbitrary, Gen }
import scala.reflect.runtime.universe.TypeTag
import scala.util.{ Failure, Try }
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
      val files = dirSource.getLines.flatMap {
        case fileName if fileName.startsWith(name) =>
          fileName.drop(name.length) match {
            case GoldenFilePattern(seed) => Some((seed, fileName))
            case _                       => None
          }
        case _ => None
      }.toList.traverse[Try, (A, String)] { case (seed, name) =>
        val contents = Resources.open(resourceRootPath + name).map { source =>
          val lines = source.getLines.mkString("\n")
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

object ResourceFileGoldenCodecLaws {
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

  def apply[A](
    size: Int = 100,
    count: Int = 1,
    printer: Printer = Printer.spaces2
  )(implicit
    decodeA: Decoder[A],
    encodeA: Encoder[A],
    arbitraryA: Arbitrary[A],
    typeTagA: TypeTag[A]
  ): GoldenCodecLaws[A] =
    apply[A](Resources.inferName[A], Resources.inferRootDir, Resources.inferPackage[A], size, count, printer)
}
