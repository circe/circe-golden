package io.circe.testing.golden

import java.io.File
import scala.io.Source
import scala.reflect.runtime.universe.TypeTag
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

    while (current.getName != "target" && current.ne(null)) {
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
    A.tpe.typeSymbol.fullName.split('.').init.toList

  /**
   * Attempt to guess the name of the type indicated by the provided type tag.
   */
  def inferName[A](implicit A: TypeTag[A]): String =
    A.tpe.typeSymbol.name.decodedName.toString

  def open(path: String): Try[Source] = Try(
    Source.fromInputStream(getClass.getResourceAsStream(path))
  )

}
