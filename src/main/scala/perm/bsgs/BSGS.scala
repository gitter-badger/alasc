package com.faacets
package perm
package bsgs

import scala.annotation.tailrec
import scala.util.Random
import language.implicitConversions
import scala.language.higherKinds


object BSGS {
  def apply[G <: PermGroup[E], E <: PermElement[E]](g: G) = {
    implicit val gen = scala.util.Random
    BSGS.randomSchreierSims(g.random, g.order, g.identity)
  }
  def randomSchreierSims[E <: PermElement[E]](randomElement: => E, order: BigInt, id: E, baseStrategy: BaseStrategy = EmptyBase, transBuilder: TransBuilderLike = ExpTransBuilder) = {
    val cons = BSGS.fromBase[E](baseStrategy.get(List(randomElement)), id, transBuilder)
    while (cons.order < order)
      cons.addElement(randomElement)
    cons.makeImmutable
    cons
  }

  def schreierSims[E <: PermElement[E]](generators: List[E], id: E, baseStrategy: BaseStrategy = EmptyBase, transBuilder: TransBuilderLike = ExpTransBuilder) = {
    val cons = BSGS.fromBaseAndGeneratingSet(baseStrategy.get(generators), generators, id, transBuilder)
    while (cons.putInOrder) { }
    cons.makeImmutable
    cons
  }

  // Internal constructions
  private[bsgs] def fromBaseAndGeneratingSet[E <: PermElement[E]](base: Base, genSet: List[E], id: E,
    transBuilder: TransBuilderLike = ExpTransBuilder): BSGSGroup[E] = {
    def create(beta: Dom, tailBase: List[Dom]) = {
      var trv = transBuilder.empty(beta, id)
      trv = trv.updated(genSet, genSet)
      new BSGSGroupNode(trv, genSet, id, false,
        fromBaseAndGeneratingSet(tailBase, genSet.filter(_.image(beta) == beta), id, transBuilder))
    }
    base match {
      case Nil => {
        val genNotIdentity = genSet.filter(!_.isIdentity)
        if (genNotIdentity.isEmpty)
          return BSGSGroupTerminal(id)
        else {
          for (g <- genNotIdentity; i <- 0 until g.size; k = Dom._0(i) if g.image(k) != k)
            return create(k, Nil)
          throw new IllegalArgumentException("Bad arguments.")
        }
      }
      case hd :: tl => create(hd, tl)
    }
  }

  private[bsgs] def fromBase[E <: PermElement[E]](base: Base, id: E,
    transBuilder: TransBuilderLike = ExpTransBuilder): BSGSGroup[E] = {

    def create(levelBase: Base): BSGSGroup[E] = levelBase match {
      case Nil => BSGSGroupTerminal(id)
      case hd :: tl => new BSGSGroupNode(transBuilder.empty(hd, id), Nil, id, false, create(tl))
    }
    if (base.isEmpty)
      create(List(Dom._0(0)))
    else
      create(base)
  }

}
