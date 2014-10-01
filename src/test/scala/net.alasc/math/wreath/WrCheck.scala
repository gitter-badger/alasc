package net.alasc.math
package wreath

import org.scalacheck._
import org.scalacheck.Arbitrary._
import org.scalatest._
import prop._
import spire.syntax.group._
import spire.syntax.groupAction._
import spire.syntax.eq._
import net.alasc.algebra._
import net.alasc.syntax.permutationAction._

class WrCheckImprimitive extends PermutationActionCheck[Wr[Perm, Perm]] {
  object PermGen extends PermutationGenerators[Perm] {
    implicit def permutation = Perm.Algebra
  }
  implicit def maximumSize = 16
  def genWr: Gen[Wr[Perm, Perm]] = for {
    k <- Gen.choose(0, 3)
    a <- Gen.containerOfN[Seq, Perm](k, PermGen.genP(4))
    h <- PermGen.genP(4)
  } yield Wr(a, h)
  implicit def arbitrary: Arbitrary[Wr[Perm, Perm]] = Arbitrary(genWr)
  implicit val action: PermutationAction[Wr[Perm, Perm]] = new WrImprimitiveRepresentations[Perm, Perm].get(Seq(Wr(Seq(Perm(0,1,2,3),Perm(0,1,2,3),Perm(0,1,2,3),Perm(0,1,2,3)), Perm(0,1,2,3)))).action
  implicit val finiteGroup: FiniteGroup[Wr[Perm, Perm]] = new WrFiniteGroup[Perm, Perm]
}

class WrCheckPrimitive extends PermutationActionCheck[Wr[Perm, Perm]] {
  object PermGen extends PermutationGenerators[Perm] {
    implicit def permutation = Perm.Algebra
  }
  implicit def maximumSize = 16
  def genWr: Gen[Wr[Perm, Perm]] = for {
    k <- Gen.choose(0, 3)
    a <- Gen.containerOfN[Seq, Perm](k, PermGen.genP(4))
    h <- PermGen.genP(4)
  } yield Wr(a, h)
  implicit def arbitrary: Arbitrary[Wr[Perm, Perm]] = Arbitrary(genWr)
  implicit val action: PermutationAction[Wr[Perm, Perm]] = new WrPrimitiveRepresentations[Perm, Perm].get(Seq(Wr(Seq(Perm(0,1,2,3),Perm(0,1,2,3),Perm(0,1,2,3),Perm(0,1,2,3)), Perm(0,1,2,3)))).action
  implicit val finiteGroup: FiniteGroup[Wr[Perm, Perm]] = new WrFiniteGroup[Perm, Perm]
}

class WrCheckInhImprimitive extends PermutationActionCheck[Wr[Perm, Perm]] {
  object PermGen extends PermutationGenerators[Perm] {
    implicit def permutation = Perm.Algebra
  }
  implicit def maximumSize = 16
  def genWr: Gen[Wr[Perm, Perm]] = for {
    k <- Gen.choose(0, 3)
    a <- Gen.containerOfN[Seq, Perm](k, PermGen.genP(4))
    h <- PermGen.genP(4)
  } yield Wr(a, h)
  implicit def arbitrary: Arbitrary[Wr[Perm, Perm]] = Arbitrary(genWr)
  implicit val action: PermutationAction[Wr[Perm, Perm]] = new InhWrImprimitiveRepresentations[Perm, Perm].get(Seq(Wr(Seq(Perm(0,1,2,3),Perm(0,1,2,3),Perm(0,1,2,3),Perm(0,1,2,3)), Perm(0,1,2,3)))).action
  implicit val finiteGroup: FiniteGroup[Wr[Perm, Perm]] = new WrFiniteGroup[Perm, Perm]
}

class WrCheckInhPrimitive extends PermutationActionCheck[Wr[Perm, Perm]] {
  object PermGen extends PermutationGenerators[Perm] {
    implicit def permutation = Perm.Algebra
  }
  implicit def maximumSize = 16
  def genWr: Gen[Wr[Perm, Perm]] = for {
    k <- Gen.choose(0, 3)
    a <- Gen.containerOfN[Seq, Perm](k, PermGen.genP(4))
    h <- PermGen.genP(4)
  } yield Wr(a, h)
  implicit def arbitrary: Arbitrary[Wr[Perm, Perm]] = Arbitrary(genWr)
  implicit val action: PermutationAction[Wr[Perm, Perm]] = new InhWrPrimitiveRepresentations[Perm, Perm].get(Seq(Wr(Seq(Perm(0,1,2,3),Perm(0,1,2,3),Perm(0,1,2,3),Perm(0,1,2,3)), Perm(0,1,2,3)))).action
  implicit val finiteGroup: FiniteGroup[Wr[Perm, Perm]] = new WrFiniteGroup[Perm, Perm]
}

object InhWrRepresentationsLattice {
  val base = new InhWrImprimitiveRepresentations[Perm, Perm]
}

class InhWrRepresentationsLattice extends BoundedBelowLatticeCheck[InhWrRepresentationsLattice.base.R] {
  implicit def lattice = InhWrRepresentationsLattice.base.lattice
  object PermGen extends PermutationGenerators[Perm] {
    implicit def permutation = Perm.Algebra
  }
  def genWr: Gen[Wr[Perm, Perm]] = for {
    k <- Gen.choose(0, 3)
    a <- Gen.containerOfN[Seq, Perm](k, PermGen.genP(4))
    h <- PermGen.genP(4)
  } yield Wr(a, h)
  implicit def arbitraryLatticeElement = Arbitrary {
    genWr.map(w => InhWrRepresentationsLattice.base.get(Iterable(w)))
  }
}