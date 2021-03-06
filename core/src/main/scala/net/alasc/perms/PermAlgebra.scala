package net.alasc.perms

import spire.algebra.{Eq, Group}

import net.alasc.algebra._
import net.alasc.perms.internal.{Perm16Encoding, Perm32Encoding}
import net.alasc.util._

object PermAlgebra extends Eq[Perm] with Group[Perm] with PermutationAction[Perm] {

  override def toString = "Perm.algebra"

  def isFaithful = true

  def eqv(x: Perm, y: Perm): Boolean = x match {
    case lhs16: Perm16 => y match {
      case rhs16: Perm16 => lhs16.encoding == rhs16.encoding
      case _ => false
    }
    case lhs32: Perm32 => y match {
      case _: Perm16 => false
      case rhs32: Perm32 =>
        lhs32.long2 == rhs32.long2 && lhs32.long1 == rhs32.long1 && lhs32.long0 == rhs32.long0
      case rhs: PermBase => rhs.genEqv(lhs32)
    }
    case lhs: PermBase => y match {
      case _: Perm16 => false
      case rhs: AbstractPerm => lhs.genEqv(rhs)
    }
  }

  def combine(x: Perm, y: Perm): Perm = x match {
    case lhs16: Perm16 => y match {
      case rhs16: Perm16 => new Perm16(Perm16Encoding.op(lhs16.encoding, rhs16.encoding))
      case rhs32: Perm32 => Perm32Encoding.op1632(lhs16, rhs32)
      case rhs: Perm if lhs16.encoding == 0L => rhs
      case rhs: PermBase => rhs.genRevOp(x)
    }
    case lhs32: Perm32 => y match {
      case rhs16: Perm16 => Perm32Encoding.op3216(lhs32, rhs16)
      case rhs32: Perm32 => Perm32Encoding.op3232(lhs32, rhs32)
      case rhs: PermBase => rhs.genRevOp(x)
    }
    case lhs: PermBase => y match {
      case Perm16(0L) => lhs
      case rhs: Perm => lhs.genOp(rhs)
    }
  }

  def findMovedPoint(p: Perm): NNOption = p.largestMovedPoint

  override def movesAnyPoint(p: Perm): Boolean = p match {
    case p16: Perm16 => !p16.isId
    case _ => true
  }

  override def movedPoints(p: Perm): Set[Int] = p.movedPoints

  override def nMovedPoints(p: Perm): Int = p.nMovedPoints

  override def smallestMovedPoint(p: Perm): NNOption = p.smallestMovedPoint

  override def largestMovedPoint(p: Perm): NNOption = p.largestMovedPoint

  def actr(preimage: Int, p: Perm): Int = p.image(preimage)

  override def actl(p: Perm, i: Int): Int = p.invImage(i)

  def inverse(p: Perm): Perm = p.inverse

  val empty = Perm16Encoding.id

  def movedPointsUpperBound(p: Perm) = largestMovedPoint(p)

}
