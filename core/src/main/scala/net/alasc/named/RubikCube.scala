package net.alasc.named

import spire.math.SafeLong

import net.alasc.algebra._
import net.alasc.finite.{Grp, GrpGroup}
import net.alasc.perms._

// http://www.gap-system.org/Doc/Examples/rubik.html
object RubikCube {

  val colors = Seq(0,
      1,1,1,1,1,1,1,1,
      2,2,2,2,2,2,2,2,
      3,3,3,3,3,3,3,3,
      4,4,4,4,4,4,4,4,
      5,5,5,5,5,5,5,5,
      6,6,6,6,6,6,6,6)

  val generators = IndexedSeq(
    Perm( 1, 3, 8, 6)( 2, 5, 7, 4)( 9,33,25,17)(10,34,26,18)(11,35,27,19),
    Perm( 9,11,16,14)(10,13,15,12)( 1,17,41,40)( 4,20,44,37)( 6,22,46,35),
    Perm(17,19,24,22)(18,21,23,20)( 6,25,43,16)( 7,28,42,13)( 8,30,41,11),
    Perm(25,27,32,30)(26,29,31,28)( 3,38,43,19)( 5,36,45,21)( 8,33,48,24),
    Perm(33,35,40,38)(34,37,39,36)( 3, 9,46,32)( 2,12,47,29)( 1,14,48,27),
    Perm(41,43,48,46)(42,45,47,44)(14,22,30,38)(15,23,31,39)(16,24,32,40))

  val order = SafeLong(BigInt("43252003274489856000"))

  def apply()(implicit gb: GrpGroup[Perm]): Grp[Perm] = Grp.fromGeneratorsAndOrder(generators, order)

}
