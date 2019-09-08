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
package asserts

import scala.quoted._
import scala.tasty._

object RecorderMacro {
  def apply[R: Type, A: Type](
      recording: Expr[R],
      listener: Expr[RecorderListener[R, A]]) given (qctx: QuoteContext): Expr[A] = {
    apply(recording, '{""}, listener)
  }

  def apply[R: Type, A: Type](
      recording: Expr[R],
      message: Expr[String],
      listener: Expr[RecorderListener[R, A]]) given (qctx: QuoteContext): Expr[A] = {
    import qctx.tasty._
    val termArg: Term = recording.unseal.underlyingArgument

    def getText(expr: Tree): String = {
      val pos = expr.pos
      (" " * pos.startColumn) + pos.sourceCode
    }

    '{
      val recorderRuntime: RecorderRuntime[R, A] = new RecorderRuntime($listener)
      recorderRuntime.recordMessage($message)
      ${
        val runtimeSym = '[RecorderRuntime[_, _]].unseal.symbol match {
          case IsClassDefSymbol(sym) => sym
        }
        val recordExpressionSel: Term = {
          val m = runtimeSym.method("recordExpression").head
          '{ recorderRuntime }.unseal.select(m)
        }
        val recordValueSel: Term = {
          val m = runtimeSym.method("recordValue").head
          '{ recorderRuntime }.unseal.select(m)
        }

        def recordExpressions(recording: Term): List[Term] = {
          val text = getText(recording)
          val ast = recording.showExtractors
          try {
            List(
              '{ recorderRuntime.resetValues() }.unseal,
              recordExpression(text, ast, recording)
            )
          } catch {
            case e: Throwable => throw new RuntimeException(
              "Expecty: Error rewriting expression.\nText: " + text + "\nAST : " + ast, e)
          }
        }

        def recordExpression(text: String, ast: String, expr: Term): Term = {
          val instrumented = recordAllValues(expr)
          Apply(recordExpressionSel,
            List(
              Literal(Constant(text)),
              Literal(Constant(ast)),
              instrumented
            ))
        }

        def recordAllValues(expr: Term): Term =
          expr match {
            case New(_)     => expr
            case Literal(_) => expr
            case Typed(r @ Repeated(xs, y), tpe) => recordSubValues(r)
            // don't record value of implicit "this" added by compiler; couldn't find a better way to detect implicit "this" than via point
            case Select(x@This(_), y) if expr.pos.start == x.pos.start => expr
            // case x: Select if x.symbol.isModule => expr // don't try to record the value of packages
            case _ => recordValue(recordSubValues(expr), expr)
          }

        def recordSubValues(expr: Term): Term =
          expr match {
            case Apply(x, ys) =>
              try {
                Apply(recordAllValues(x), ys.map(recordAllValues))
              } catch {
                case e: AssertionError => expr
              }
            // case TypeApply(x, ys) => recordValue(TypeApply.copy(expr)(recordSubValues(x), ys), expr)
            case TypeApply(x, ys) => TypeApply.copy(expr)(recordSubValues(x), ys)
            case Select(x, y)     => Select.copy(expr)(recordAllValues(x), y)
            case Typed(x, tpe)    => Typed.copy(expr)(recordSubValues(x), tpe)
            case Repeated(xs, y)  => Repeated.copy(expr)(xs.map(recordAllValues), y)
            case _                => expr
          }

        def recordValue(expr: Term, origExpr: Term): Term = {
          // debug
          // println("recording " + expr.showExtractors + " at " + getAnchor(expr))

          def skipIdent(sym: Symbol): Boolean =
            sym.fullName match {
              case "scala" | "java" => true
              case fullName if fullName.startsWith("scala.") => true
              case fullName if fullName.startsWith("java.")  => true
              case _ => false
            }

          def skipSelect(sym: Symbol): Boolean =
            (sym match {
              case IsDefDefSymbol(sym) => sym.signature.paramSigs.nonEmpty
              case IsValDefSymbol(sym) => skipIdent(sym)
              case _ => true
            })
          expr match {
            case Select(_, _) if skipSelect(expr.symbol) => expr
            case TypeApply(_, _) => expr
            case Ident(_) if skipIdent(expr.symbol) => expr
            case _ =>
              val tapply = recordValueSel.appliedToType(expr.tpe)
              Apply.copy(expr)(
                tapply,
                List(
                  expr,
                  Literal(Constant(getAnchor(expr)))
                )
              )
          }
        }

        def getAnchor(expr: Term): Int =
          expr match {
            case Apply(x, ys) if x.symbol.fullName == "verify.asserts.RecorderRuntime.recordValue" && ys.nonEmpty =>
              getAnchor(ys.head)
            case Apply(x, ys)     => getAnchor(x) + 0
            case TypeApply(x, ys) => getAnchor(x) + 0
            case Select(x, y)     =>
              expr.pos.startColumn + math.max(0, expr.pos.sourceCode.indexOf(y))
            case _                => expr.pos.startColumn
          }
        Block(
          recordExpressions(termArg),
          '{ recorderRuntime.completeRecording() }.unseal
        ).seal.cast[A]
      }
    }
  }
}
