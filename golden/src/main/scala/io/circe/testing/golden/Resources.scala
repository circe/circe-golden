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

import java.io.File
import scala.io.Source
import scala.util.Try

/**
 * Miscellaneous utilities for guessing resource locations
 */
object Resources {

  /**
   * Attempt to guess the test resource root directory for the current project, creating it if it does not exist.
   */
  def inferRootDir(cls: Class[_]): File = {
    var current = new File(cls.getProtectionDomain.getCodeSource.getLocation.getFile)

    while (current.ne(null) && current.getName != "target")
      current = current.getParentFile

    val resourceDir = new File(new File(new File(current.getParentFile, "src"), "test"), "resources")

    resourceDir.mkdirs()
    resourceDir
  }

  def open(path: String): Try[Source] = Try(
    Source.fromInputStream(getClass.getResourceAsStream(path))
  )

}
