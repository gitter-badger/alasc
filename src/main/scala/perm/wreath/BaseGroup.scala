package com.faacets
package perm
package wreath

import scala.util.Random

/** Represents the base group of a wreath product group.
  * 
  * It is the direct product of a group G n times with itself.
  */
class BaseGroup[F <: FiniteElement[F] : Manifest, A <: FiniteGroup[F, A]](a: A, n: Int) extends
    FiniteGroup[BaseElement[F], BaseGroup[F, A]] {
  override def toString = "" + n + " direct copies of " + a.toString
  def compatible(e: BaseElement[F]) = e.arr.size == n
  def contains(e: BaseElement[F]) = {
    require_(compatible(e))
    true
  }
  def iteratorOverCopies(d: Int): Iterator[List[F]] = d match {
    case 0 => List(List.empty[F]).iterator
    case _ => for {
      f <- a.elements
      rest <- iteratorOverCopies(d - 1)
    } yield (f :: rest)
  }
  def elements = iteratorOverCopies(n).map(list => new BaseElement(list.toArray))
  def identity = new BaseElement(Array.tabulate(n)(i => a.identity))
  def generators = for {
    k <- (0 until n).iterator
    g <- a.generators
  } yield new BaseElement(identity.arr.updated(k, g))
  def order = a.order.pow(n)
  def random(implicit gen: Random) = new BaseElement(Array.tabulate(n)(i => a.random))
}

class BaseElement[F <: FiniteElement[F] : Manifest](val arr: Array[F]) extends FiniteElement[BaseElement[F]] {
  override def toString = arr.mkString("(",",",")")
  def *(that: BaseElement[F]) = {
    val newArr = new Array[F](arr.size)
    for (i <- 0 until arr.size) newArr(i) = arr(i)*that.arr(i)
    new BaseElement(newArr)
  }
  def compatible(that: BaseElement[F]) = arr.size == that.arr.size
  def equal(that: BaseElement[F]) = (0 until arr.size).forall( i => arr(i).equal(that.arr(i)))
  def inverse = {
    val newArr = new Array[F](arr.size)
    for (i <- 0 until arr.size) newArr(i) = arr(i).inverse
    new BaseElement(newArr)
  }
  def isIdentity = (0 until arr.size).forall( i => arr(i).isIdentity )
}
