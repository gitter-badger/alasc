package net.alasc.laws

import spire.algebra.{Action, Eq}

import org.scalacheck.{Arbitrary, Gen}
import shapeless.Witness

import net.alasc.domains._

object Domains {

  def sized: Gen[Domain] = Gen.posNum[Int].map(Domain(_))

  implicit def arbDomain: Arbitrary[Domain] = Arbitrary(sized)

}
