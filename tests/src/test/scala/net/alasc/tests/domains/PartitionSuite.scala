package net.alasc
package domains

import scala.collection.SortedSet

import spire.laws.LatticeLaws

import net.alasc.laws.{AnyRefLaws, Domains, Partitions}
import net.alasc.tests.AlascSuite

class PartitionSuite extends AlascSuite {

  import Domains.arbDomain

  nestedCheckAll[Domain]("Domain.Partition", Domain(1)) { d =>
    import Partitions.arbPartitionIn
    val domain: Domain = d
    LatticeLaws[Partition.In[domain.type]].boundedLattice
  }

  {
    import Partitions.{arbPartition, partitionCloner, partitionInstances}
    checkAll("Partition", AnyRefLaws[Partition]._eq)
  }

  test("Partition bug") {
    val t1: Partition = Partition.fromSortedBlocks(Domain(8))(Seq(SortedSet(0,5,6,2,4),SortedSet(1,3,7)))
    val t2 = Partition(Set(0,5,6,2,4),Set(1,3,7))
    t1.toString should === (t2.toString)
  }

}
