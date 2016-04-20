package net.alasc.laws

import org.scalacheck.{Arbitrary, Gen}

import net.alasc.finite._

object Grps {

  def genRandomElement[G](grp: Grp[G]): Gen[G] = Gen.parameterized { params => grp.randomElement(params.rng) }

  def genSubgrp[G:GrpBuilder](grp: Grp[G]): Gen[Grp[G]] =
    fromElements(genRandomElement(grp))

  def fromElements[G:GrpBuilder](elements: Gen[G]): Gen[Grp[G]] =
    for {
      n <- Gen.choose(0, 4)
      generators <- Gen.containerOfN[Seq, G](n, elements)
      c <- elements
    } yield Grp(generators: _*).conjugatedBy(c)

  implicit def arbGrp[G:Arbitrary:GrpBuilder]: Arbitrary[Grp[G]] = 
    Arbitrary {
      Gen.parameterized { parameters =>
        val gSize = math.max(parameters.size / 10, 3)
        val elements = Gen.resize(gSize, implicitly[Arbitrary[G]].arbitrary)
        for {
          c <- implicitly[Arbitrary[G]].arbitrary
          grp <- fromElements(elements)
        } yield grp.conjugatedBy(c)
      }
    }

  def arbSubgrp[GG <: Grp[G] with Singleton, G:GrpBuilder](implicit witness: shapeless.Witness.Aux[GG]): Arbitrary[Grp[G]] =
    Arbitrary(genSubgrp(witness.value: GG))

  implicit def instances[G:Instances:GrpBuilder]: Instances[Grp[G]] =
    Instances[G].map(Grp(_))

}