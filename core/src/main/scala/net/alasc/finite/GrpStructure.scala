package net.alasc.finite

import scala.annotation.tailrec
import scala.reflect.ClassTag

import spire.algebra.{Eq, Group, Semigroup}
import spire.math.SafeLong
import spire.syntax.cfor._
import spire.syntax.eq._
import spire.syntax.group._
import spire.util.Opt

trait GrpStructure[G] {

  implicit def grpGroup: GrpGroup[G]

  import Grp.Attributes

  protected implicit def equ: Eq[G]

  protected implicit def group: Group[G]

  def smallGeneratingSet(grp: Grp[G]): IndexedSeq[G] = {
    def computeOrder(gens: IndexedSeq[G]): SafeLong = grpGroup.fromGenerators(gens).order
    GrpStructure.deterministicReduceGenerators(grp.generators, grp.order, computeOrder).getOrElseFast(grp.generators)
  }

  def commutator(subgrp1: Grp[G], subgrp2: Grp[G]): Grp[G] = {
    // implementation according to Prop. 2.35 p.28 of [Holt2005]
    val comSubgrpGens = for {
      g1 <- subgrp1.generators
      g2 <- subgrp2.generators
      g = net.alasc.finite.commutator(g1, g2) if !g.isId
    } yield g
    grpGroup.fromGenerators(comSubgrpGens)
  }

  def derivedSubgroup(grp: Grp[G]): Grp[G] = Attributes.DerivedSubgroup(grp) {
    commutator(grp, grp)
  }

  def isAbelian(grp: Grp[G]): Boolean = Attributes.IsAbelian(grp) {
    GrpStructure.isCommutativeFromGenerators[G](grp.generators)
  }

}

object GrpStructure {

  def isCommutativeFromGenerators[G:Eq:Semigroup](generators: IndexedSeq[G]): Boolean = {
    cforRange(1 until generators.length) { i =>
      cforRange(0 until i) { j =>
        if ((generators(i) |+| generators(j)) =!= (generators(j) |+| generators(i)))
          return false
      }
    }
    false
  }

  /** Try to remove generators one by one. */
  def deterministicReduceGenerators[G:Eq:Group](generators: IndexedSeq[G], order: SafeLong, computeOrder: IndexedSeq[G] => SafeLong, min: Int = 1)(): Opt[IndexedSeq[G]] = {
    @tailrec def rec(current: IndexedSeq[G], i: Int, ret: Opt[IndexedSeq[G]]): Opt[IndexedSeq[G]] =
      if (i == current.length || generators.length <= min) ret else {
        val test = current.patch(i, Nil, 1)
        if (computeOrder(test) === order)
          rec(test, i, Opt(test))
        else
          rec(current, i + 1, ret)
      }
    rec(generators, 0, Opt.empty[IndexedSeq[G]])
  }

}

class GrpStructureSyntax[G](val lhs: Grp[G]) extends AnyVal {

  /** Simplifies the description current group.*/
  def smallGeneratingSet(implicit ev: GrpStructure[G]): IndexedSeq[G] = ev.smallGeneratingSet(lhs)

  /** Returns the subgroup of `grp` generated by all the commutators of the group, i.e.
    * commutator(g1, g2) for g1, g2 in G. */
  def derivedSubgroup(implicit ev: GrpStructure[G]): Grp[G] = ev.commutator(lhs, lhs)

  /** Returns the commutator group generated by grp1 and grp2, i.e.
    * commutator(h1, h2) for h1 in grp1 and h2 in grp2.
    */
  def commutator(grp1: Grp[G], grp2: Grp[G])(implicit ev: GrpStructure[G]): Grp[G] = ev.commutator(grp1, grp2)

}
