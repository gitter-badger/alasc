package com.faacets.perm

package wreath {
  import com.faacets.math._

  case class ImprimitiveInhWreathProductGroup[R <: InhWreathProductGroup](r: R)(implicit evidence: R#A <:< PermutationGroup) extends ActionGroup {
    type Group = ImprimitiveInhWreathProductGroup[R]
    type Element = ImprimitiveInhWreathProductAction

    type RepresentedGroup = R

    def make(h: RepresentedElement) = ImprimitiveInhWreathProductAction(h)

    def degree = r.avec.map(_.degree).sum

    def order = r.order

    def identity = make(r.identity)
    def randomElement = make(r.randomElement)
    def assertValid = r.assertValid
    def contains(a: Element) = r.contains(a.g)
    def generators = r.generators.map(make(_))
    def elements = r.elements.map(make(_))

    case class ImprimitiveInhWreathProductAction(val g: RepresentedElement) extends Action {
      self: Element =>
      val group = ImprimitiveInhWreathProductGroup.this
      def image(el: Domain) = {
        val gaelvec = g.aelvec.asInstanceOf[Vector[PermutationGroup#Permutation]]
        val sizes = gaelvec.map(_.domainSize)
        val aStart = sizes.scanLeft(0)(_+_)
        val i = aStart.zipWithIndex.find(_._1 > el).get._2 - 1
        val o = el - aStart(i)
        val i1 = g.hel.image(i)
        val o1 = gaelvec(i1).image(o)
        aStart(i1) + o1
      }
      lazy val images = {
        val gaelvec = g.aelvec.asInstanceOf[Vector[PermutationGroup#Permutation]]
        val P = scala.collection.mutable.ArrayBuffer.fill[Int](domainSize)(0)
        val sizes = gaelvec.map(_.domainSize)
        val aStart = sizes.scanLeft(0)(_+_)
        for ((n, i) <- sizes.zipWithIndex) {
          for (o <- 0 until n) {
            val i1 = g.hel.image(i)
            val o1 = gaelvec(i1).image(o)
            P(aStart(i) + o) = aStart(i1) + o1
          }
        }
        P.toVector
      }
    }
  }
}
