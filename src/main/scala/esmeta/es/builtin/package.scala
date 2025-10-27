package esmeta.es.builtin

import esmeta.es.*
import esmeta.state.*
import esmeta.spec.*
import esmeta.cfg.CFG
import esmeta.ty.*
import scala.collection.mutable.{Map => MMap}

/** predefined shortcuts */
val T = true
val F = false
val U = Undef

/** predefined constants */
val INTRINSICS = "INTRINSICS"
val TYPED_ARRAY = "TYPED_ARRAY"
val GLOBAL = "GLOBAL"
val SYMBOL = "SYMBOL"
val MATH_PI = "MATH_PI"
val AGENT_RECORD = "AGENT_RECORD"
val AGENT_SIGNIFIER = "AGENT_SIGNIFIER"
val CANDIDATE_EXECUTION = "CANDIDATE_EXECUTION"
val KEPT_ALIVE = "KEPT_ALIVE"
val REALM = "REALM"
val EXECUTION_STACK = "EXECUTION_STACK"
val HOST_DEFINED = "HOST_DEFINED"
val JOB_QUEUE = "JOB_QUEUE"
val PRIMITIVE = "PRIMITIVE"
val SOURCE_TEXT = "SOURCE_TEXT"
val SYMBOL_REGISTRY = "SYMBOL_REGISTRY"
val BOUND_VALUE = "__BOUND_VALUE__"
val RESUME_CONT = "__RESUME_CONT__"
val RETURN_CONT = "__RETURN_CONT__"
val INNER_CODE = "__CODE__"
val INNER_MAP = "__MAP__"
val PRIVATE_ELEMENTS = "PrivateElements"
val DESCRIPTOR = "DESCRIPTOR"
val UNDEF_TYPE = "Undefined"
val NULL_TYPE = "Null"
val BOOL_TYPE = "Boolean"
val STRING_TYPE = "String"
val SYMBOL_TYPE = "Symbol"
val NUMBER_TYPE = "Number"
val BIGINT_TYPE = "BigInt"
val OBJECT_TYPE = "Object"

/** not yet supported objects */
val yets: Map[String, ValueTy] =
  Map(
    // 21. Numbers and Dates
    "Date" -> ConstructorT,
    // 22. Text Processing
    "RegExp" -> ConstructorT,
    // 25. Structured Data
    "SharedArrayBuffer" -> ConstructorT,
    "DataView" -> ConstructorT,
    "Atomics" -> ObjectT,
    "JSON" -> ObjectT,
    // 26. Managing Memory
    "WeakRef" -> ConstructorT,
    "FinalizationRegistry" -> ConstructorT,
    // test262
    "$262" -> ConstructorT,
    // ShadowRealm
    "ShadowRealm" -> ObjectT,
  )

/** not yet supported functions */
val yetFuncs: Set[String] = Set(
  "String.prototype.localeCompare",
  "String.prototype.toLocaleLowerCase",
  "Number.prototype.toLocaleString",
  "String.prototype.toUpperCase",
  "String.prototype.toLocaleUpperCase",
)

// https://tc39.es/ecma262/#table-well-known-intrinsic-objects
val WELL_KNOWN_INTRINSICS = "table-well-known-intrinsic-objects"

// https://tc39.es/ecma262/#sec-well-known-symbols
val WELL_KNOWN_SYMBOLS = "table-well-known-symbols"

// https://tc39.es/ecma262/#table-well-known-intrinsic-objects
val TYPED_ARRAY_CONSTRUCTORS = "table-the-typedarray-constructors"

// address for the current realm
val realmAddr = NamedAddr(REALM)

/** intrinsics name */
def intrName(name: String): String = s"$INTRINSICS.$name"

/** intrinsics addr */
def intrAddr(name: String): NamedAddr = NamedAddr(intrName(name))

/** typed array name */
def taName(name: String): String = s"$TYPED_ARRAY.$name"

/** typed array addr */
def taAddr(name: String): NamedAddr = NamedAddr(taName(name))

/** map name */
def mapName(name: String): String = s"$name.$INNER_MAP"

/** map addr */
def mapAddr(name: String): NamedAddr = NamedAddr(mapName(name))

/** PrivateElements name */
def elemsName(name: String): String = s"$name.$PRIVATE_ELEMENTS"

/** PrivateElements addr */
def elemsAddr(name: String): NamedAddr = NamedAddr(elemsName(name))

/** symbol name */
def symbolName(name: String): String = s"Symbol.$name"

/** symbol address */
def symbolAddr(name: String): NamedAddr = intrAddr(symbolName(name))

/** descriptor name */
def descName(name: String, key: PropKey): String = key match
  case PropKey.Str(x) => s"$DESCRIPTOR.$name.$x"
  case PropKey.Sym(x) => s"$DESCRIPTOR.$name[%Symbol.$x%]"

/** descriptor address */
def descAddr(name: String, key: PropKey): NamedAddr =
  NamedAddr(descName(name, key))

/** get map */
def getMapObjects(
  name: String,
  descBase: String,
  nmap: List[(PropKey, PropDesc)],
)(using CFG): Map[Addr, Obj] =
  var map = Map[Addr, Obj]()
  map += mapAddr(name) -> MapObj(nmap.map { (k, _) =>
    val key = k match
      case (PropKey.Str(x)) => Str(x)
      case (PropKey.Sym(x)) => symbolAddr(x)
    key -> descAddr(descBase, k)
  })
  map ++= nmap.map { case (k, d) => descAddr(descBase, k) -> toObject(d) }
  map

/** convert property descriptor to ir map object */
def toObject(desc: PropDesc)(using CFG): RecordObj = desc match
  case DataDesc(v, w, e, c) =>
    recordObj("PropertyDescriptor")(
      "Value" -> v,
      "Writable" -> Bool(w),
      "Enumerable" -> Bool(e),
      "Configurable" -> Bool(c),
    )
  case AccessorDesc(g, s, e, c) =>
    recordObj("PropertyDescriptor")(
      "Get" -> g,
      "Set" -> s,
      "Enumerable" -> Bool(e),
      "Configurable" -> Bool(c),
    )
