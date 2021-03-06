package squid.lang

import scala.reflect.runtime.universe.TypeTag

/** The base trait defining, in the tagless final style, the core type language of Squid:
  *   type application, constant type and uninterpreted types if supported for those that do not fall into these categories. */
trait TypingBase { self: Base =>
  
  /** Internal, untype representation of code type */
  type TypeRep <: AnyRef  // AnyRef bound so it can be used in squid.utils.Lazy (see EmbeddedType.asStaticallyAppliedType)
  
  def uninterpretedType[A: TypeTag]: TypeRep
  def typeApp(self: TypeRep, typ: TypSymbol, targs: List[TypeRep]): TypeRep // make targs a vararg?
  def staticTypeApp(typ: TypSymbol, targs: List[TypeRep]): TypeRep 
  def recordType(fields: List[(String, TypeRep)]): TypeRep
  def constType(value: Any, underlying: TypeRep): TypeRep
  
  type TypSymbol
  def loadTypSymbol(fullName: String): TypSymbol 
  
  def typLeq(a: TypeRep, b: TypeRep): Boolean
  def typEq(a: TypeRep, b: TypeRep): Boolean = typLeq(a,b) && typLeq(b,a)
  
  
  final def staticModuleType(fullName: String): TypeRep = staticTypeApp(loadTypSymbol(fullName+"$"), Nil)
  
  implicit class TypingRepOps(private val self: TypeRep) {
    def <:< (that: TypeRep) = typLeq(self, that)
    def =:= (that: TypeRep) = typEq(self, that)
  }
  
  
}
