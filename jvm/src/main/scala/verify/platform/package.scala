/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package verify

/**
 * The `platform` package provides the required Scala types for
 * source-level compatibility between JVM/JS and Native, along with
 * utilities with a platform-specific implementation.
 */
package object platform {

  /**
   * Type alias needed because Scala Native does not provide
   * the standard [[scala.concurrent.Future]] class yet.
   */
  type Future[+A] = scala.concurrent.Future[A]

  /**
   * Type alias needed because Scala Native does not provide
   * the standard [[scala.concurrent.Future]] class yet.
   */
  val Future = scala.concurrent.Future

  /**
   * Type alias needed because Scala Native does not provide
   * an implementation for [[scala.concurrent.Await]] yet.
   */
  val Await = scala.concurrent.Await

  /**
   * Type alias needed because Scala Native does not provide
   * an implementation for [[scala.concurrent.Promise]] yet.
   */
  type Promise[A] = scala.concurrent.Promise[A]

  /**
   * Type alias needed because Scala Native does not provide
   * an implementation for [[scala.concurrent.Promise]] yet.
   */
  val Promise = scala.concurrent.Promise

  /**
   * Type alias needed because Scala Native does not provide
   * an implementation for [[scala.concurrent.ExecutionContext]] yet.
   */
  type ExecutionContext = scala.concurrent.ExecutionContext

  /**
   * Type alias needed because Scala Native does not provide
   * an implementation for [[scala.concurrent.ExecutionContext]] yet.
   */
  val ExecutionContext = scala.concurrent.ExecutionContext

  type EnableReflectiveInstantiation = verify.internal.EnableReflectiveInstantiation

  private[verify] def loadModule(name: String, loader: ClassLoader): Any = {
    verify.internal.Reflect.loadModule(name, loader)
  }
}
