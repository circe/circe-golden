package io.circe.testing.golden

import java.io.File
import scala.io.Source
import scala.reflect.runtime.universe.{ Symbol, Type, TypeTag }
import scala.util.Try

/**
 * Miscellaneous utilities for guessing resource locations, names, etc.
 */
object Resources {

  /**
   * Attempt to guess the test resource root directory for the current project, creating it if it
   * does not exist.
   */
  lazy val inferRootDir: File = {
    var current = new File(getClass.getResource("/").toURI)

    while (current.ne(null) && current.getName != "target") {
      current = current.getParentFile
    }

    val resourceDir = new File(new File(new File(current.getParentFile, "src"), "test"), "resources")

    resourceDir.mkdirs()
    resourceDir
  }

  /**
   * Attempt to guess the packaging of the type indicated by the provided type tag.
   */
  def inferPackage[A](implicit A: TypeTag[A]): List[String] =
    owners(A.tpe).collectFirst { case s if s.isPackage => s.fullName.split('.').toList }.getOrElse(List.empty)

  private def owners(tpe: Type): Iterator[Symbol] =
    Iterator.iterate(tpe.typeSymbol)(_.owner)

  /**
   * Attempt to guess the name of the type indicated by the provided type tag.
   */
  def inferName[A](implicit A: TypeTag[A]): String = inferNameForType(A.tpe)

  private def baseSymbols(tpe: Type): List[Symbol] =
    owners(tpe).takeWhile(!_.isPackage).toList.reverse

  private def inferNameForType(tpe: Type): String = {
    val baseNames = baseSymbols(tpe).map(_.name.decodedName.toString)

    (baseNames ::: tpe.typeArgs.map(inferNameForType)).mkString("_")
  }

  def open(path: String): Try[Source] = Try(
    Source.fromInputStream(getClass.getResourceAsStream(path))
  )

}
