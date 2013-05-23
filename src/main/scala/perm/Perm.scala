package com.faacets
package perm

import scala.util.Random

object Perm {
  def apply(n: Int) = new Perm((0 until n).toArray)
  def fromImages(imgs: Dom*) = new Perm(imgs.map(_._0).toArray)
  def apply(images: DomArray) = new Perm(images.array.asInstanceOf[Array[Int]])
}

class Perm(val arr: Array[Int]) extends AnyVal with PermElement[Perm] {
  def isIdentity: Boolean = domain.forall( k => k == image(k) )
  def size = arr.size
  def toTeX = TeX("{}^"+arr.size)+TeX(cycles.filter(_.size>1).map(_.mkString("(",",",")")).mkString(""))
  def invImage(k: Dom): Dom = {
    var i = 0
    while (i < arr.size) {
      if (arr(i) == k._0)
        return Dom._0(i)
      i += 1
    }
    throw new IllegalArgumentException("Permutation should contain the image")
  }
  def image(k: Dom) = Dom._0(arr(k._0))
  def images: DomArray = DomArray.zeroBased(arr)
  def compatible(that: Perm) = size == that.size
  def explicit = this
  // note that the image of b under the product g*h is given by:
  // b^(g*h) = (b^g)^h
  def *(that: Perm): Perm = {
    require_(compatible(that))
    val a = new Array[Int](size)
    for (i <- 0 until size) a(i) = that.arr(arr(i))
    new Perm(a)
  }
  def inverse: Perm = {
    val a = new Array[Int](size)
    for (i <- 0 until size) a(arr(i)) = i
    new Perm(a)
  }
  def equal(that: Perm) = {
    require_(compatible(that))
    arr.sameElements(that.arr)
  }
  def withSwap(i: Dom, j: Dom): Perm = {
    require_(isDefinedAt(i) && isDefinedAt(j))
    val a = arr.clone
    val k = a(i._0)
    a(i._0) = a(j._0)
    a(j._0) = k
    new Perm(a)
  }
  def apply(s: String): Perm = {
    val points = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val list = s.map( c => Dom._0(points.indexOf(c)) )
    apply(list:_*)
  }
  def apply(cycle: Dom*): Perm = {
    val a = arr.clone
    val a0 = a(cycle(0)._0)
    for (i <- 0 until cycle.size - 1)
      a(cycle(i)._0) = a(cycle(i + 1)._0)
    a(cycle(cycle.size-1)._0) = a0
    new Perm(a)
  }
}
