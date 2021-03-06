package net.alasc.bsgs

import scala.reflect.ClassTag

import spire.algebra.{Eq, Group}
import spire.syntax.action._
import spire.syntax.group._
import spire.util.Opt

import net.alasc.algebra.PermutationAction
import net.alasc.syntax.group._

/** Defines the subgroup which intersect `chain2`, given in action `action` (not necessarily faithful). */
case class Intersection[G:ClassTag:Eq:Group, A <: PermutationAction[G] with Singleton]
  (chain2: Chain[G, A], commonKernel: Chain.Generic[G])
  (implicit val action: A, baseChange: BaseChange, schreierSims: SchreierSims) extends SubgroupDefinition[G, A] {

  def baseGuideOpt = Opt(BaseGuideSeq(chain2.base))

  def inSubgroup(g: G): Boolean = chain2.sift(g) match {
    case Opt(rem1) => commonKernel.sift(rem1) match {
      case Opt(rem2) => rem2.isId
      case _ => false

    }
    case _ => false
  }

  class Test(level: Int, currentChain2: Chain[G, A], prev2Inv: G) extends SubgroupTest[G, A] {

    def test(b: Int, orbitImage: Int, currentG: G, node: Node[G, A]): Opt[Test] = {
      val b2 = orbitImage <|+| prev2Inv
      currentChain2 match {
        case node2: Node[G, A] if node2.inOrbit(b2) =>
          Opt(new Test(level + 1, node2.next, prev2Inv |+| node2.uInv(b2)))
        case _ if node.beta == b2 =>
          Opt(new Test(level + 1, currentChain2, prev2Inv))
        case _ =>
          Opt.empty[Test]
      }
    }

  }

  def firstLevelTest(chain1: Chain[G, A]): Test =
    new Test(0, chain2, Group[G].id)

}
