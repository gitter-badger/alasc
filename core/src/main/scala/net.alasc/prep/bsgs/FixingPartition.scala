package net.alasc.prep.bsgs

import scala.reflect.ClassTag

import spire.algebra.{Eq, Group}
import spire.syntax.group._
import spire.syntax.action._
import spire.util.Opt

import net.alasc.algebra.FaithfulPermutationAction
import net.alasc.domains.Partition
import net.alasc.util._

case class FixingPartition[G:Group](val action: FaithfulPermutationAction[G], val partition: Partition) extends SubgroupDefinition[G] {

  val n = partition.size

  def inSubgroup(g: G): Boolean = {
    var i = 0
    while (i < n) {
      if (partition.representative(action.actr(i, g)) != partition.representative(i))
        return false
      i += 1
    }
    true
  }

  def baseGuideOpt = Opt(BaseGuidePartition(partition))

    // TODO: change pointSetsToTest to bitsets
  class Test(level: Int, pointSetsToTest: Array[Array[Int]]) extends SubgroupTest[G] {

    def test(b: Int, orbitImage: Int, currentG: G, node: Node[G]): Opt[Test] = {
      val pointSet = pointSetsToTest(level)
      if (partition.representative(pointSet(0)) != partition.representative(orbitImage))
        return Opt.empty[Test]
      if (pointSet.length > 1) {
        val nextG = node.u(b) |+| currentG
        var i = 1
        while (i < pointSet.length) {
          val k = pointSet(i)
          if (partition.representative(k) != partition.representative(action.actr(k, nextG)))
            return Opt.empty[Test]
          i += 1
        }
      }
      Opt(new Test(level + 1, pointSetsToTest))
    }
  }

  def firstLevelTest(guidedChain: Chain[G]): Test = {
    val pointSetsToTest: Array[Array[Int]] =
      SubgroupSearch.basePointGroups(guidedChain, n)
    new Test(0, pointSetsToTest)
  }

}