package net.alasc.bsgs

import scala.util.Random

import spire.algebra.Group
import spire.syntax.action._
import spire.syntax.group._

import metal.syntax._

import net.alasc.algebra._

/** Contains information about a transversal in a BSGS chain. */  
trait Transversal[G, F <: FaithfulPermutationAction[G] with Singleton] extends net.alasc.finite.Transversal[G] {

  def beta: Int

  def inOrbit(b: Int): Boolean

  def orbitSize: Int
  def orbitIterator: Iterator[Int]

  def foreachOrbit(f: Int => Unit): Unit

  def orbit: Iterable[Int] = new Iterable[Int] {
    override def size = orbitSize
    override def stringPrefix = "Iterable"
    override def foreach[U](f: Int => U) = foreachOrbit { k => f(k) }
    def iterator = orbitIterator
  }

  def randomOrbit(rand: Random): Int = orbit.drop(rand.nextInt(orbitSize)).head

  def orbitMin: Int = {
    var m = beta
    foreachOrbit { k =>
      if (k < m) m = k
    }
    m
  }

  def orbitMax: Int = {
    var m = beta
    foreachOrbit { k =>
      if (k > m) m = k
    }
    m
  }

  def orbitSet: Set[Int] = {
    val bitset = metal.mutable.BitSet.empty
    foreachOrbit { bitset += _ }
    bitset.toScala
  }

  def foreachU(f: G => Unit): Unit
  def u(b: Int): G
  def uInv(b: Int): G
  def randomU(rand: Random): G

}

object Transversal {

  def empty[G:Group, F <: FaithfulPermutationAction[G] with Singleton](beta: Int): Transversal[G, F] =
    new EmptyTransversal[G, F](beta)

}

case class ConjugatedTransversal[G:Group, F <: FaithfulPermutationAction[G] with Singleton]
  (originalTransversal: Transversal[G, F], g: G, gInv: G)(implicit action: F) extends Transversal[G, F] {

  def elements = originalTransversal.elements.map(u => gInv |+| u |+| g)
  def elementFor(h: G) = gInv |+| originalTransversal.elementFor(g |+| h |+| gInv) |+| g
  def orbitSize = originalTransversal.orbitSize
  def beta = originalTransversal.beta <|+| g
  def inOrbit(b: Int) = originalTransversal.inOrbit(b <|+| gInv)
  def orbitIterator = originalTransversal.orbitIterator.map(b => b <|+| g)
  def foreachOrbit(f: Int => Unit) = originalTransversal.foreachOrbit(b => f(b <|+| g))
  def foreachU(f: G => Unit): Unit = originalTransversal.foreachU(u => f(gInv |+| u |+| g))
  def u(b: Int) = gInv |+| originalTransversal.u(b <|+| gInv) |+| g
  def uInv(b: Int) = g |+| originalTransversal.uInv(b <|+| gInv) |+| gInv
  def randomU(rand: Random) = gInv |+| originalTransversal.randomU(rand) |+| g

}

class EmptyTransversal[G:Group, F <: FaithfulPermutationAction[G] with Singleton](val beta: Int)
  extends Transversal[G, F] {

  def elements = Iterable(Group[G].id)
  def elementFor(g: G) = Group[G].id
  def orbitSize = 1
  def inOrbit(b: Int) = beta == b
  def orbitIterator = Iterator(beta)
  def foreachOrbit(f: Int => Unit) = { f(beta) }
  def foreachU(f: G => Unit) = { f(Group[G].id) }
  def u(b: Int) = { require(b == beta); Group[G].id }
  def uInv(b: Int) = u(b)
  def randomU(rand: Random) = Group[G].id

}
