package io.circe.testing.golden

import cats.instances.list._, cats.instances.try_._
import cats.syntax.traverse._
import cats.laws._
import io.circe.{ Json, Printer }
import io.circe.testing.CodecLaws
import scala.util.{ Failure, Success, Try }

trait GoldenCodecLaws[A] extends CodecLaws[A] {

  /**
   * Printer used to serialize a [[Json]] value as a string.
   *
   * We use `spaces2` by default because it provides useful diffs and human-readable examples, but
   * you can override if space or some other consideration is a priority.
   */
  protected def printer: Printer = Printer.spaces2

  final protected def printJson(value: Json): String = printer.print(value)

  /**
   * A list of pairs of values and their JSON encodings serialized to strings (or an error).
   *
   * We use `Try` in order to provide better error messages in tests in the case that golden file
   * resources are unavailable or misconfigured.
   */
  protected def goldenExamples: Try[List[(A, String)]]

  final def goldenDecoding: Try[List[IsEq[A]]] = goldenExamples.flatMap {
    _.traverse { case (value, encoded) =>
      io.circe.parser.decode[A](encoded)(decode) match {
        case Left(error)    => Failure(error)
        case Right(decoded) => Success(decoded <-> value)
      }
    }
  }

  final def goldenEncoding: Try[List[IsEq[String]]] = goldenExamples.map {
    _.map { case (value, encoded) =>
      printJson(encode(value)) <-> encoded
    }
  }
}
