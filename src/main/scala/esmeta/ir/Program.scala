package esmeta.ir

import esmeta.*
import esmeta.es.builtin.Intrinsics
import esmeta.ir.util.{Parser, YetCollector}
import esmeta.parser.{ESParser, AstFrom}
import esmeta.spec.Spec
import esmeta.ty.TyModel
import esmeta.util.BaseUtils.*
import esmeta.util.ProgressBar
import esmeta.util.SystemUtils.*

/** IR programs */
case class Program(
  funcs: List[Func] = Nil, // IR functions
) extends IRElem {

  /** backward edge to a specification */
  var spec: Spec = Spec()

  /** the main function */
  lazy val main: Func = getUnique(funcs, _.main, "main function")

  /** ECMAScript parser */
  lazy val esParser: ESParser = spec.esParser
  lazy val scriptParser: AstFrom = esParser("Script")

  /** get list of all yet expressions */
  lazy val yets: List[(EYet, Func)] = for {
    func <- funcs
    yet <- func.yets
  } yield (yet, func)

  /** complete functions */
  lazy val completeFuncs: List[Func] = funcs.filter(_.complete)

  /** incomplete functions */
  lazy val incompleteFuncs: List[Func] = funcs.filter(!_.complete)

  /** get the type model */
  lazy val tyModel: TyModel = spec.tyModel

  /** get the intrinsics */
  lazy val intrinsics: Intrinsics = spec.intrinsics

  /** dump IR program */
  def dumpTo(baseDir: String, loc: Boolean = false): Unit =
    val dirname = s"$baseDir/func"
    dumpDir(
      name = "IR functions",
      iterable = ProgressBar("Dump IR functions", funcs, detail = false),
      dirname = dirname,
      getName = func => s"${func.name}.ir",
      getData = func => func.toString(detail = true, location = loc),
    )
}
object Program extends Parser.From(Parser.program) {
  def apply(funcs: List[Func], spec: Spec): Program =
    val program = Program(funcs)
    program.spec = spec
    program
}
