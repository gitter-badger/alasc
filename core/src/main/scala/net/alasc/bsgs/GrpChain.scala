package net.alasc.bsgs

import scala.annotation.tailrec
import scala.reflect.ClassTag

import spire.algebra.{Group, Order}
import spire.math.SafeLong
import spire.syntax.action._
import spire.syntax.group._
import spire.util.Opt

import net.alasc.algebra.{BigIndexedSeq, FaithfulPermutationAction, PermutationAction}
import net.alasc.bsgs
import net.alasc.domains.{MutableOrbit, Partition}
import net.alasc.finite.{Grp, LeftCoset, LeftCosets, LeftCosetsImpl}

// TODO: move to bsgs
abstract class GrpChain[G, F <: FaithfulPermutationAction[G] with Singleton] extends Grp[G] { lhs =>

  implicit def action: F

  implicit def classTag: ClassTag[G]

  def chain: bsgs.Chain[G, F]

  def chainOpt: Opt[bsgs.Chain[G, F]]

}

object GrpChain {

  def subgroupFor[G, F <: FaithfulPermutationAction[G] with Singleton]
    (grp: GrpChain[G, F], definition: SubgroupDefinition[G, F])
    (implicit baseChange: BaseChange, schreierSims: SchreierSims): GrpChain[G, F] = {
    import grp.{action, classTag, equ, group}
    val guidedChain = grp match {
      case lhs: GrpChainConjugated[G, F] =>
        import lhs.{g, gInv, originalChain}
        val mut = originalChain.mutableChain
        mut.conjugate(g, gInv)
        definition.baseGuideOpt match {
          case Opt(baseGuide) => baseChange.changeBase(mut, baseGuide)
          case _ =>
        }
        mut.toChain()
      case _ =>
        BuildChain.fromChain[G, F, F](grp.chain, definition.baseGuideOpt)
    }
    val subChain = SubgroupSearch.subgroupSearch(definition, guidedChain).toChain()
    new GrpChainExplicit[G, F](subChain)
  }

  def fixingPartition[G, F <: FaithfulPermutationAction[G] with Singleton]
    (grp: GrpChain[G, F], partition: Partition)(implicit baseChange: BaseChange, schreierSims: SchreierSims): GrpChain[G, F] = {
    import grp.{action, group}
    subgroupFor(grp, net.alasc.bsgs.FixingPartition[G, F](partition))
  }


  def someStabilizerTransversal[G, F <: FaithfulPermutationAction[G] with Singleton]
    (grp: GrpChain[G, F]): Opt[(GrpChain[G, F], Transversal[G, F])] = {
    import grp.{action, classTag, equ, group}
    grp match {
      case conj: GrpChainConjugated[G, F] => conj.originalChain match {
        case node: Node[G, F] =>
          val nextGrp = new GrpChainConjugated(node.next, conj.g, conj.gInv)
          val trv = ConjugatedTransversal(node, conj.g, conj.gInv)
          Opt((nextGrp, trv))
        case _ => Opt.empty[(GrpChain[G, F], Transversal[G, F])]
      }
      case _ => grp.chain match {
        case node: Node[G, F] => Opt((new GrpChainExplicit(node.next), node))
        case _ => Opt.empty[(GrpChain[G, F], Transversal[G, F])]
      }
    }
  }

  def stabilizerTransversal[G, F <: FaithfulPermutationAction[G] with Singleton]
    (grp: GrpChain[G, F], b: Int)
    (implicit baseChange: BaseChange, schreierSims: SchreierSims): (GrpChain[G, F], Transversal[G, F]) = {
    // duplicated code with stabilizer below
    import grp.{action, classTag, equ, group}
    grp match {
      case conj: GrpChainConjugated[G, F] =>
        import conj.{g, gInv, originalChain}
        originalChain match {
          case node: Node[G, F] =>
            val a = b <|+| gInv
            if (node.inOrbit(a)) {
              val u = node.u(a)
              val uInv = node.uInv(a)
              val newG = u |+| g
              val newGInv = gInv |+| uInv
              val nextGrp = new GrpChainConjugated[G, F](node.next, newG, newGInv)
              val trv = ConjugatedTransversal[G, F](node, newG, newGInv)
              (nextGrp, trv)
            }
            else if (node.isFixed(a))
              (conj, Transversal.empty[G, F](b))
            else {
              // TODO: optimize single element base change
              val newChain = BuildChain.fromChain[G, F, F](originalChain, Opt(BaseGuideSeq(Seq(a))))
              val (nextOriginalChain, originalTransversal) = newChain.detach(a)
              val nextGrp = new GrpChainConjugated[G, F](nextOriginalChain, g, gInv)
              val trv = ConjugatedTransversal[G, F](originalTransversal, g, gInv)
              (nextGrp, trv)
            }
          case term: Term[G, F] => (conj, Transversal.empty[G, F](b))
        }
      case _ => grp.chain match {
        case term: Term[G, F] => (grp, Transversal.empty[G, F](b))
        case node: Node[G, F] if node.inOrbit(b) =>
          val u = node.u(b)
          val uInv = node.uInv(b)
          val nextGrp = new GrpChainConjugated[G, F](node.next, u, uInv)
          val trv: Transversal[G, F] = ConjugatedTransversal[G, F](node, u, uInv)
          (nextGrp, trv)
        case node: Node[G, F] if node.isFixed(b) =>
          (grp, Transversal.empty[G, F](b))
        case _ =>
          // TODO: optimize single element base change
          val newChain = BuildChain.fromChain[G, F, F](grp.chain, Opt(BaseGuideSeq(Seq(b))))
          val (nextChain, trv) = newChain.detach(b)
          val nextGrp = new GrpChainExplicit[G, F](nextChain)
          (nextGrp, trv)
      }
    }
  }

  def stabilizer[G, F <: FaithfulPermutationAction[G] with Singleton]
    (grp: GrpChain[G, F], b: Int)
    (implicit baseChange: BaseChange, schreierSims: SchreierSims): GrpChain[G, F] = {
    // duplicated code with stabilizerTransversal above
    import grp.{action, classTag, equ, group}
    grp match {
      case conj: GrpChainConjugated[G, F] =>
        import conj.{g, gInv, originalChain}
        originalChain match {
          case node: Node[G, F] =>
            val a = b <|+| gInv
            if (node.inOrbit(a)) {
              val u = node.u(a)
              val uInv = node.uInv(a)
              val newG = u |+| g
              val newGInv = gInv |+| uInv
              new GrpChainConjugated[G, F](node.next, newG, newGInv)
            }
            else if (node.isFixed(a))
              conj
            else {
              // TODO: optimize single element base change
              val newChain = BuildChain.fromChain[G, F, F](originalChain, Opt(BaseGuideSeq(Seq(a))))
              val (nextOriginalChain, originalTransversal) = newChain.detach(a)
              new GrpChainConjugated[G, F](nextOriginalChain, g, gInv)
            }
          case term: Term[G, F] => conj
        }
      case _ => grp.chain match {
        case term: Term[G, F] => grp
        case node: Node[G, F] if node.inOrbit(b) =>
          val u = node.u(b)
          val uInv = node.uInv(b)
          new GrpChainConjugated[G, F](node.next, u, uInv)
        case node: Node[G, F] if node.isFixed(b) => grp
        case _ =>
          // TODO: optimize single element base change
          val newChain = BuildChain.fromChain[G, F, F](grp.chain, Opt(BaseGuideSeq(Seq(b))))
          val (nextChain, _) = newChain.detach(b)
          new GrpChainExplicit[G, F](nextChain)
      }
    }
  }

  def pointwiseStabilizer[G, F <: FaithfulPermutationAction[G] with Singleton]
    (grp: GrpChain[G, F], set: Set[Int])(implicit baseChange: BaseChange): GrpChain[G, F] = {
    import grp.{action, classTag, equ, group}
    val guide = PointwiseStabilizer.baseGuide(set)
    val guidedChain = grp match {
      case conj: GrpChainConjugated[G, F] =>
        import conj.{g, gInv}
        val mut = conj.originalChain.mutableChain
        mut.conjugate(g, gInv)
        baseChange.changeBase(mut, guide)
        mut.toChain()
      case _ =>
        val mut = grp.chain.mutableChain
        baseChange.changeBase(mut, guide)
        mut.toChain()
    }
    new GrpChainExplicit[G, F](PointwiseStabilizer.recurse(guidedChain, set))
  }

  def leftCosetsBy[G, F <: FaithfulPermutationAction[G] with Singleton]
    (grp0: GrpChain[G, F], subgrp0: GrpChain[G, F])
    (implicit baseChange: BaseChange, schreierSims: SchreierSims): LeftCosets[G] = {
    import grp0.{action, classTag, group}
    require(grp0.hasSubgroup(subgrp0)) // TODO: add NC variant
    new LeftCosetsImpl[G] {

      val grp = grp0

      val subgrp = subgrp0

      private[this] val n = PermutationAction.largestMovedPoint(grp.generators).getOrElse(0) + 1

      def iterator: Iterator[LeftCoset[G]] = {
        import grp.{action, group}
        val myBase = grp.chain.base
        val bo = BaseOrder[G, F](myBase)
        val bordering = Order.ordering(bo)
        val orbit = MutableOrbit.forSize(n)
        import spire.std.int.IntAlgebra
        def rec(g: G, chain: Chain[G, F], subSubgrp: GrpChain[G, F]): Iterator[LeftCoset[G]] = chain match {
          case node: Node[G, F] =>
            for {
              b <- node.orbit.iterator
              bg = b <|+| g if MutableOrbit.isSmallestPointInOrbit(n, bg, subSubgrp.generators, orbit)
              nextSubSubgrp = stabilizer[G, F](subSubgrp, bg)
              nextG = node.u(b) |+| g
              element <- rec(nextG, node.next, nextSubSubgrp)
            } yield element
          case _: Term[G, F] =>
            assert(pointwiseStabilizer(subgrp, myBase.map(_ <|+| g).toSet).isTrivial)
            assert(subSubgrp.order == 1)
            Iterator(new LeftCoset(g, subgrp))
        }
        rec(Group[G].id, grp.chain, subgrp)
      }

    }

  }

  final class LexElements[G, F <: FaithfulPermutationAction[G] with Singleton]
    (grp: GrpChain[G, F])(implicit baseChange: BaseChange, schreierSims: SchreierSims) extends BigIndexedSeq[G] {

    import grp.{action, classTag, equ, group}

    val lexChain: Chain[G, F] = if (grp.chain.hasLexicographicBase) grp.chain else {
      val n = PermutationAction.largestMovedPoint(grp.generators).getOrElse(0) + 1
      BuildChain.fromChain(grp.chain, Opt(BaseGuideLex(n)))
    }

    def length = lexChain.order

    def contains(g: G) = lexChain.sifts(g)

    def apply(idx: SafeLong): G = {
      @tailrec def rec(current: Chain[G, F], curIdx: SafeLong, curOrder: SafeLong, curG: G): G = current match {
        case node: Node[G, F] =>
          val sortedOrbit = node.orbit.toSeq.sortBy(k => k <|+| curG)
          val nextOrder = curOrder / node.orbitSize
          val nextIdx = curIdx % nextOrder
          val orbitIndex = ((curIdx - nextIdx) / nextOrder).toInt
          val nextG = node.u(sortedOrbit(orbitIndex)) |+| curG
          rec(node.next, nextIdx, nextOrder, nextG)
        case _: Term[G, F] =>
          assert(curIdx == 0)
          curG
      }
      rec(lexChain, idx, lexChain.order, Group[G].id)
    }

    def iterator: Iterator[G] = {
      def rec(current: Chain[G, F], curG: G): Iterator[G] = current match {
        case node: Node[G, F] =>
          val sortedOrbit = node.orbit.toSeq.sortBy(k => k <|+| curG)
          for {
            b <- sortedOrbit.iterator
            nextG = node.u(b) |+| curG
            rest <- rec(node.next, nextG)
          } yield rest
        case _: Term[G, F] => Iterator(curG)
      }
      rec(lexChain, Group[G].id)
    }

  }

}