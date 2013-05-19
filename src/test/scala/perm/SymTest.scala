package com.faacets
package perm

import org.scalacheck._
import com.faacets.perm._
import scala.util.Random

object SymGenerators {
  implicit val random = Random
  val genSym = for {
    degree <- Gen.choose(1, 20)
  } yield Sym(degree)

  val genSymAndElement = for {
    s <- genSym
  } yield (s, s.random)

  val genSymAndTwoElements = for {
    s <- genSym
  } yield (s, s.random, s.random)
}

object SymSpecification extends Properties("Sym") {
  import SymGenerators._
  property("contains/identity") = Prop.forAll(genSym) { s: Sym => s.contains(s.identity) }
  property("fromExplicit/equal") = Prop.forAll(genSymAndElement) { Function.tupled(
    (s, e) => s.fromExplicit(e.explicit).get.equal(e) 
  ) }
}