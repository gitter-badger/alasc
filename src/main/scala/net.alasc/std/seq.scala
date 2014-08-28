package net.alasc.std

import spire.algebra._
import net.alasc.algebra._
import scala.collection.SeqLike
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

class SeqSequence[SA <: SeqLike[A, SA], A] extends Sequence[SA, A] {
  def length(s: SA) = s.length
  def elemAt(s: SA, i: Int): A = s(i)
  def toIndexedSeq(s: SA): IndexedSeq[A] = s.toIndexedSeq
}

class SeqPermutationAction[SA <: SeqLike[A, SA], A, P: FiniteGroup: FaithfulPermutationAction](
  implicit cbf: CanBuildFrom[Nothing, A, SA]) extends GroupAction[SA, P] {
  import net.alasc.syntax.permutationAction._
  import spire.syntax.group._
  import spire.syntax.groupAction._

  def actl(p: P, s: SA): SA = {
    val b = cbf()
    b.sizeHint(s)
    for (i <- 0 until s.length)
      b += s(i <|+| p)
    b.result
  }

  def actr(s: SA, p: P): SA = actl(p.inverse, s)
}

trait SeqInstances0 {
  implicit def SeqSequence[CC[A] <: SeqLike[A, CC[A]], A]: Sequence[CC[A], A] = new SeqSequence[CC[A], A]
  implicit def SeqPermutationAction[CC[A] <: SeqLike[A, CC[A]], A, P: FiniteGroup: FaithfulPermutationAction](
    implicit cbf: CanBuildFrom[Nothing, A, CC[A]]) = new SeqPermutationAction[CC[A], A, P]
}

trait SeqInstances extends SeqInstances0 {

}
