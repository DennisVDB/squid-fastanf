package squid
package classembed

import utils._
import squid.ir._
import squid.lang.ScalaCore
import squid.utils.meta.RuntimeUniverseHelpers
import squid.quasi.{embed,dbg_embed,phase}

import DirectSymbolAccessTests._

@embed
class TestClass {
  def foo = 123
}
object TestClass {
  def bar = "ok"
  def overloaded(x:Int) = 1
  def overloaded(x:Bool) = 2
}

@embed[TestClass.Lang]
object UserTestClass {
  @phase('Sugar) def bar2 = TestClass.bar
}

class DirectSymbolAccessTests extends MyFunSuite(LocalTestDSL) {
  import LocalTestDSL.Predef._
  
  test("Methods Loading Count") {
    
    assert(mtdLoadCount == Map())
    
    assert(ir"TestClass.bar".run == "ok")
    
    assert(mtdLoadCount("bar") == 1)
    
    assert(ir"TestClass.bar + 1".run == "ok1")
    
    assert(mtdLoadCount("bar") == 1)
    
    assert(!mtdLoadCount.contains("foo"))
    
    assert(ir"(new TestClass).foo".run == 123)
    assert(ir"(tc:TestClass) => tc.foo+1".run apply (new TestClass) equals 124)
    
    assert(mtdLoadCount("foo") == 1)
    
    
    // Testing @embed-generated quasiquotes:
    
    def q0 = ir"UserTestClass.bar2.length" transformWith LocalTestDSL.Desugaring
    
    assert(mtdLoadCount("bar") == 1)
    
    q0 eqt ir"TestClass.bar.length" // Inlining `UserTestClass.bar2` uses the ir{...} quasicode generated by 
    // the `@embed[TestClass.Lang]` on `UserTestClass`; here we check that the quasicode was indeed compiled using
    // direct symbol access, so that the method load count for `bar` does not increase!
    
    assert(mtdLoadCount("bar") == 1)
    
    q0 eqt ir"TestClass.bar.length"
    
    assert(mtdLoadCount("bar") == 1)
    
  }
  
  test("Overloaded Methods") {
    
    assert(ir"TestClass.overloaded(0)".run == 1)
    
    assert(ir"TestClass.overloaded(true)".run == 2)
    
  }
  
}

object DirectSymbolAccessTests {
  
  val mtdLoadCount = collection.mutable.Map[String,Int]()
  
  object LocalTestDSL extends SimpleAST with ScalaCore with TestClass.Lang with ClassEmbedder {
    embed(UserTestClass)
    override def loadMtdSymbol(typ: ScalaTypeSymbol, symName: String, index: Option[Int], static: Bool): MtdSymbol = {
      mtdLoadCount(symName) = mtdLoadCount.getOrElse(symName, 0) + 1
      super.loadMtdSymbol(typ, symName, index, static)
    }
  }
  
}
