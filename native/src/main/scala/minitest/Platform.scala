/*
 * Copyright (c) 2014-2018 by The Minitest Project Developers.
 * Some rights reserved.
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

package minitest

import scala.scalanative.testinterface.PreloadedClassLoader

trait Platform

object Platform extends Platform {
  type EnableReflectiveInstantiation =
    scala.scalajs.reflect.annotation.EnableReflectiveInstantiation

  private[minitest] def loadModule(name: String, loader: ClassLoader): Any =
    loader.asInstanceOf[PreloadedClassLoader].loadPreloaded(name)
}
