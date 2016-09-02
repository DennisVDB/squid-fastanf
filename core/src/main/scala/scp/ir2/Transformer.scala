package scp
package ir2

import scp.lang2.Optimizer
import utils._

trait Transformer extends Optimizer { self =>
  val base: lang2.InspectableBase
  import base._
  
  object TranformerDebug extends PublicTraceDebug
  
  def transform(rep: Rep): Rep
  final def transformTopDown(rep: Rep): Rep = (base topDown rep)(transform)
  final def transformBottomUp(rep: Rep): Rep = (base bottomUp rep)(transform)
  
  final def pipeline = transform
  
  def andThen(that: Transformer{val base: self.base.type}): Transformer{val base: self.base.type} = new Transformer {
    val base: self.base.type = self.base
    import base._
    def transform(rep: Rep): Rep = that transform self.transform(rep)
  }
  
}

trait TopDownTransformer extends Transformer { abstract override def transform(rep: base.Rep) = (base topDown rep)(super.transform) }
trait BottomUpTransformer extends Transformer { abstract override def transform(rep: base.Rep) = (base bottomUp rep)(super.transform) }
